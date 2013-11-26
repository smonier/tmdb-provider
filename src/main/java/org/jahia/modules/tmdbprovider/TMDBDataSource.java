package org.jahia.modules.tmdbprovider;

import com.google.common.collect.Sets;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.jahia.modules.external.ExternalData;
import org.jahia.modules.external.ExternalDataSource;
import org.jahia.modules.external.ExternalQuery;
import org.jahia.modules.external.query.QueryHelper;
import org.jahia.services.cache.ehcache.EhCacheProvider;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.jcr.*;
import java.util.*;
import java.util.regex.Pattern;

public class TMDBDataSource implements ExternalDataSource, ExternalDataSource.LazyProperty, ExternalDataSource.Searchable {
    public static final HashSet<String> LAZY_PROPERTIES = Sets.newHashSet("original_title", "homepage", "status", "runtime", "imdb_id", "budget", "revenue");
    public static final HashSet<String> LAZY_I18N_PROPERTIES = Sets.newHashSet("jcr:title", "overview", "tagline", "poster_path");

    public static final HashSet<String> ROOT_NODES = Sets.newHashSet("movies", "lists");

    private static String API_URL = "api.themoviedb.org";
    private static String API_CONFIGURATION = "/3/configuration";
    private static String API_MOVIE = "/3/movie/";
    private static String API_TV = "/3/tv/";
    private static String API_DISCOVER_MOVIE = "/3/discover/movie";
    private static String API_DISCOVER_TV = "/3/discover/tv";
    private static String API_SEARCH_MOVIE = "/3/search/movie";
    private static String API_KEY = "api_key";

    private static Pattern YEAR_PATTERN = Pattern.compile("[0-9]{4,4}");
    private static Pattern DATE_PATTERN = Pattern.compile("[0-9]{4,4}/[0-9]{2,2}");

    private EhCacheProvider ehCacheProvider;
    private Ehcache cache;
    private String apiKeyValue;

    private String accountId;
    private String token;
    private String sessionId;

    private HttpClient httpClient;

    public TMDBDataSource() {
        httpClient = new HttpClient();
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void setCacheProvider(EhCacheProvider ehCacheProvider) {
        this.ehCacheProvider = ehCacheProvider;
    }

    public void setApiKeyValue(String apiKeyValue) {
        this.apiKeyValue = apiKeyValue;
    }

    public void start() {
        try {
            if (!ehCacheProvider.getCacheManager().cacheExists("tmdb-cache")) {
                ehCacheProvider.getCacheManager().addCache("tmdb-cache");
            }
            cache = ehCacheProvider.getCacheManager().getCache("tmdb-cache");
        } catch (IllegalStateException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (CacheException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    /**
     * @param path path where to get children
     * @return list of paths as String
     */
    @Override
    public List<String> getChildren(String path) throws RepositoryException {
        List<String> r = new ArrayList<String>();

        String[] splitPath = path.split("/");
        try {
            if (splitPath.length == 0) {
                r.addAll(ROOT_NODES);
                return r;
            } else if (splitPath[1].equals("movies")) {
                switch (splitPath.length) {
                    case 2:
                        for (int i = 1900; i <= 2013; i++) {
                            r.add(Integer.toString(i));
                        }
                        return r;
                    case 3:
                        for (int i = 1; i <= 12; i++) {
                            r.add(StringUtils.leftPad(Integer.toString(i), 2, "0"));
                        }
                        return r;
                    case 4:
                        final String date = splitPath[2] + "-" + splitPath[3];
                        if (cache.get("movies-folder-" + date) != null) {
                            r = (List<String>) cache.get("movies-folder-" + date).getObjectValue();
                        } else {
                            JSONObject o = queryTMDB(API_DISCOVER_MOVIE, "release_date.gte", date + "-01", "release_date.lte", splitPath[2] + "-" + splitPath[3] + "-31");
                            JSONArray result = o.getJSONArray("results");
                            for (int i = 0; i < result.length(); i++) {
                                JSONObject movie = result.getJSONObject(i);
                                r.add(movie.getString("id"));
                                cache.put(new Element("movie-" + movie.getString("id"), movie.toString()));
                            }
                            cache.put(new Element("movies-folder-" + date, r));
                        }
                        return r;
                    case 5:
                        return Collections.emptyList();
                }
            } else if (splitPath[1].equals("lists")) {
                switch (splitPath.length) {
                    case 2:
                        if (cache.get("lists") != null) {
                            r = (List<String>) cache.get("lists").getObjectValue();
                        } else {
                            JSONObject o = queryTMDB("/3/account/" + getAccountId() + "/lists", "session_id", getSessionId());
                            JSONArray result = o.getJSONArray("results");
                            for (int i = 0; i < result.length(); i++) {
                                JSONObject list = result.getJSONObject(i);
                                r.add(list.getString("id"));
                                cache.put(new Element("list-" + list.getString("id"), list.toString()));
                            }
                            cache.put(new Element("lists", r));
                        }
                        return r;
                    case 3:
                        JSONObject list;
                        if (cache.get("fulllist-" + splitPath[2]) != null) {
                            list = new JSONObject((String) cache.get("fulllist-" + splitPath[2]).getObjectValue());
                        } else {
                            list = queryTMDB("/3/list/" + splitPath[2]);
                            cache.put(new Element("fulllist-" + splitPath[2], list.toString()));
                        }
                        JSONArray result = list.getJSONArray("items");
                        for (int i = 0; i < result.length(); i++) {
                            JSONObject movie = result.getJSONObject(i);
                            r.add(movie.getString("id"));
                            cache.put(new Element("movieref-" + movie.getString("id"), movie.toString()));
                        }
                        return r;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    /**
     * identifier is unique for an ExternalData
     *
     * @param identifier
     * @return ExternalData defined by the identifier
     * @throws javax.jcr.ItemNotFoundException
     *
     */
    @Override
    public ExternalData getItemByIdentifier(String identifier) throws ItemNotFoundException {
        try {
            if (identifier.equals("root")) {
                return new ExternalData(identifier, "/", "jnt:contentFolder", new HashMap<String, String[]>());
            }
            if (identifier.contains("-rootfolder")) {
                final String s = StringUtils.substringBefore(identifier, "-rootfolder");
                if (ROOT_NODES.contains(s)) {
                    return new ExternalData(identifier, "/" + s, "jnt:contentFolder", new HashMap<String, String[]>());
                }
            } else if (identifier.contains("-folder-")) {
                final String s = StringUtils.substringBefore(identifier, "-folder-");
                final String date = StringUtils.substringAfter(identifier, "-folder-");
                if (ROOT_NODES.contains(s) && (YEAR_PATTERN.matcher(date).matches() || DATE_PATTERN.matcher(date).matches())) {
                    return new ExternalData(identifier, "/" + s + "/" + date, "jnt:contentFolder", new HashMap<String, String[]>());
                }
            } else if (identifier.startsWith("movies-")) {
                String movieId = StringUtils.substringAfter(identifier, "movies-");
                try {
                    Integer.parseInt(movieId);
                } catch (NumberFormatException e) {
                    throw new ItemNotFoundException(identifier);
                }
                JSONObject movie;

                String lang = "en";

                if (cache.get("movie-" + movieId) != null) {
                    movie = new JSONObject((String) cache.get("movie-" + movieId).getObjectValue());
                } else if (cache.get("fullmovie-" + lang + "-" + movieId) != null) {
                    movie = new JSONObject((String) cache.get("fullmovie-" + lang + "-" + movieId).getObjectValue());
                } else {
                    movie = queryTMDB(API_MOVIE + movieId, "language", lang);
                    cache.put(new Element("fullmovie-" + lang + "-" + movieId, movie.toString()));
                }

                JSONObject configuration = getConfiguration();
                String baseUrl = configuration.getJSONObject("images").getString("base_url");

                Map<String, String[]> properties = new HashMap<String, String[]>();
                if (movie.getString("backdrop_path") != null)
                    properties.put("backdrop_path", new String[]{baseUrl + configuration.getJSONObject("images").getJSONArray("backdrop_sizes").get(1) + movie.getString("backdrop_path")});
                if (movie.getString("release_date") != null)
                    properties.put("release_date", new String[]{movie.getString("release_date") + "T00:00:00.000+00:00"});
                if (movie.getString("adult") != null) properties.put("adult", new String[]{movie.getString("adult")});
                if (movie.getString("vote_average") != null)
                    properties.put("vote_average", new String[]{movie.getString("vote_average")});
                if (movie.getString("vote_count") != null)
                    properties.put("vote_count", new String[]{movie.getString("vote_count")});
                if (movie.getString("popularity") != null)
                    properties.put("popularity", new String[]{movie.getString("popularity")});

                ExternalData data = new ExternalData(identifier, getPathForMovie(movie), "jnt:movie", properties);

                data.setLazyProperties(new HashSet<String>(LAZY_PROPERTIES));

                Map<String, Set<String>> lazy18 = new HashMap<String, Set<String>>();
                lazy18.put("en", new HashSet<String>(LAZY_I18N_PROPERTIES));
                lazy18.put("fr", new HashSet<String>(LAZY_I18N_PROPERTIES));
                data.setLazyI18nProperties(lazy18);

                return data;
            } else if (identifier.startsWith("tv-")) {
                queryTMDB(API_MOVIE + StringUtils.substringAfter(identifier, "tv-"));
            } else if (identifier.startsWith("lists-")) {
                String listId = StringUtils.substringAfter(identifier, "lists-");
                if (!Pattern.compile("[a-z0-9]+").matcher(listId).matches()) {
                    throw new ItemNotFoundException(identifier);
                }

                JSONObject list;

                if (cache.get("list-" + listId) != null) {
                    list = new JSONObject((String) cache.get("list-" + listId).getObjectValue());
                } else if (cache.get("fulllist-" + listId) != null) {
                    list = new JSONObject((String) cache.get("fulllist-" + listId).getObjectValue());
                } else {
                    list = queryTMDB("/3/list/" + listId);
                    cache.put(new Element("fulllist-" + listId, list.toString()));
                }

                JSONObject configuration = getConfiguration();
                String baseUrl = configuration.getJSONObject("images").getString("base_url");

                Map<String, String[]> properties = new HashMap<String, String[]>();
                if (list.getString("name") != null)
                    properties.put("jcr:title", new String[]{list.getString("name")});
                if (list.getString("description") != null)
                    properties.put("jcr:description", new String[]{list.getString("description")});
                if (list.getString("poster_path") != null)
                    properties.put("poster_path", new String[]{baseUrl + configuration.getJSONObject("images").getJSONArray("poster_sizes").get(1) + list.getString("poster_path")});

                ExternalData data = new ExternalData(identifier, "/lists/" + listId, "jnt:moviesList", properties);
                return data;
            } else if (identifier.startsWith("movieref-")) {
                String movieId = StringUtils.substringAfter(identifier, "movieref-");
                String listId = StringUtils.substringBefore(movieId, "-");
                movieId = StringUtils.substringAfter(movieId, "-");
                try {
                    Integer.parseInt(movieId);
                } catch (NumberFormatException e) {
                    throw new ItemNotFoundException(identifier);
                }
                if (!Pattern.compile("[a-z0-9]+").matcher(listId).matches()) {
                    throw new ItemNotFoundException(identifier);
                }

                Map<String, String[]> properties = new HashMap<String, String[]>();
                properties.put("j:node", new String[]{"movies-" + movieId});
                ExternalData data = new ExternalData(identifier, "/lists/"+listId+"/"+movieId, "jnt:contentReference", properties);
                return data;
            }
        } catch (Exception e) {
            throw new ItemNotFoundException(e);
        }

        throw new ItemNotFoundException(identifier);
    }

    private String getPathForMovie(JSONObject movie) throws JSONException {
        return "/movies/" + StringUtils.substringBeforeLast(movie.getString("release_date"), "-").replace("-", "/") + "/" + movie.getString("id");
    }

    /**
     * As getItemByIdentifier, get an ExternalData by its path
     *
     * @param path
     * @return ExternalData
     * @throws javax.jcr.PathNotFoundException
     *
     */
    @Override
    public ExternalData getItemByPath(String path) throws PathNotFoundException {
        String[] splitPath = path.split("/");
        try {
            if (splitPath.length == 0) {
                return getItemByIdentifier("root");
            } else if (splitPath[1].equals("movies")) {
                switch (splitPath.length) {
                    case 2:
                        return getItemByIdentifier(splitPath[1] + "-rootfolder");
                    case 3:
                        return getItemByIdentifier(splitPath[1] + "-folder-" + splitPath[2]);
                    case 4:
                        return getItemByIdentifier(splitPath[1] + "-folder-" + splitPath[2] + "/" + splitPath[3]);
                    case 5:
                        return getItemByIdentifier(splitPath[1] + "-" + splitPath[4]);
                }
            } else if (splitPath[1].equals("lists")) {
                switch (splitPath.length) {
                    case 2:
                        return getItemByIdentifier(splitPath[1] + "-rootfolder");
                    case 3:
                        return getItemByIdentifier(splitPath[1] + "-" + splitPath[2]);
                    case 4:
                        return getItemByIdentifier("movieref-" + splitPath[2] + "-" + splitPath[3]);
                }
            }
        } catch (ItemNotFoundException e) {
            throw new PathNotFoundException(e);
        }
        throw new PathNotFoundException();
    }

    /**
     * Returns a set of supported node types.
     *
     * @return a set of supported node types
     */
    @Override
    public Set<String> getSupportedNodeTypes() {
        return Sets.newHashSet("jnt:contentFolder", "jnt:movie", "jnt:moviesList");
    }

    /**
     * Indicates if this data source has path-like hierarchical external identifiers, e.g. IDs that are using file system paths.
     *
     * @return <code>true</code> if this data source has path-like hierarchical external identifiers, e.g. IDs that are using file system
     *         paths; <code>false</code> otherwise.
     */
    @Override
    public boolean isSupportsHierarchicalIdentifiers() {
        return false;
    }

    /**
     * Indicates if the data source supports UUIDs.
     *
     * @return <code>true</code> if the data source supports UUIDs
     */
    @Override
    public boolean isSupportsUuid() {
        return false;
    }

    /**
     * Returns <code>true</code> if an item exists at <code>path</code>; otherwise returns <code>false</code>.
     *
     * @param path item path
     * @return <code>true</code> if an item exists at <code>path</code>; otherwise returns <code>false</code>
     */
    @Override
    public boolean itemExists(String path) {
        return false;
    }

    private JSONObject queryTMDB(String path, String... params) throws RepositoryException {
        try {
            HttpURL url = new HttpURL(API_URL, 80, path);

            Map<String, String> m = new LinkedHashMap<String, String>();
            for (int i = 0; i < params.length; i += 2) {
                m.put(params[i], params[i + 1]);
            }
            m.put(API_KEY, apiKeyValue);

            url.setQuery(m.keySet().toArray(new String[m.size()]), m.values().toArray(new String[m.size()]));
            System.out.println("########" + url);
            GetMethod httpMethod = new GetMethod(url.toString());
            httpClient.executeMethod(httpMethod);
            return new JSONObject(httpMethod.getResponseBodyAsString());
        } catch (Exception e) {
            throw new RepositoryException(e);
        }
    }


    @Override
    public String[] getPropertyValues(String path, String propertyName) throws PathNotFoundException {
        return getI18nPropertyValues(path, "en", propertyName);
    }

    @Override
    public String[] getI18nPropertyValues(String path, String lang, String propertyName) throws PathNotFoundException {
        String result;
        try {
            JSONObject movie;
            if (path.startsWith("/movies")) {
                String movieId = StringUtils.substringAfterLast(path, "/");
                if (cache.get("fullmovie-" + lang + "-" + movieId) != null) {
                    movie = new JSONObject((String) cache.get("fullmovie-" + lang + "-" + movieId).getObjectValue());
                } else {
                    movie = queryTMDB(API_MOVIE + movieId, "language", lang);
                    cache.put(new Element("fullmovie-" + lang + "-" + movieId, movie.toString()));
                }
                if (propertyName.equals("jcr:title") && movie.getString("title") != null) {
                    return new String[]{movie.getString("title")};
                } else if (propertyName.equals("poster_path") && movie.getString("poster_path") != null) {
                    JSONObject configuration = getConfiguration();
                    String baseUrl = configuration.getJSONObject("images").getString("base_url");
                    return new String[]{baseUrl + configuration.getJSONObject("images").getJSONArray("poster_sizes").get(1) + movie.getString(propertyName)};
                } else if (movie.getString(propertyName) != null) {
                    return new String[]{movie.getString(propertyName)};
                }
                return new String[]{""};
            }
        } catch (JSONException e) {
            throw new PathNotFoundException(e);
        } catch (RepositoryException e) {
            throw new PathNotFoundException(e);
        }
        return new String[0];
    }

    @Override
    public Binary[] getBinaryPropertyValues(String path, String propertyName) throws PathNotFoundException {
        return new Binary[0];
    }

    public JSONObject getConfiguration() throws JSONException, RepositoryException {
        JSONObject configuration;
        if (cache.get("configuration") != null) {
            configuration = new JSONObject((String) cache.get("configuration").getObjectValue());
        } else {
            configuration = queryTMDB(API_CONFIGURATION);
            cache.put(new Element("configuration", configuration.toString()));
        }

        return configuration;
    }

    @Override
    public List<String> search(ExternalQuery query) throws RepositoryException {
        List<String> results = new ArrayList<String>();
        String nodeType = QueryHelper.getNodeType(query.getSource());

        try {
            if (nodeType.equals("jnt:movie")) {
                JSONArray tmdbResult = null;
                String year;
                String month;
                String path = QueryHelper.getRootPath(query.getConstraint());
                if (path != null) {
                    String[] splitPath = path.split("/");
                }

                Map<String, Value> m = QueryHelper.getSimpleAndConstraints(query.getConstraint());
                if (m.containsKey("jcr:title")) {
                    tmdbResult = queryTMDB(API_SEARCH_MOVIE, "query", m.get("jcr:title").getString()).getJSONArray("results");
                } else {
                    queryTMDB(API_DISCOVER_MOVIE, "query", m.get("jcr:title").getString()).getJSONArray("results");
                }

                if (tmdbResult != null) {
                    for (int i = 0; i < tmdbResult.length(); i++) {
                        results.add(getPathForMovie(tmdbResult.getJSONObject(i)));
                    }
                }
            } else if (nodeType.equals("jnt:moviesList")) {
                JSONObject o = queryTMDB("/3/account/" + getAccountId() + "/lists", "session_id", getSessionId());
                JSONArray result = o.getJSONArray("results");
                for (int i = 0; i < result.length(); i++) {
                    JSONObject list = result.getJSONObject(i);
                    results.add("/lists/" + list.getString("id"));
                }
            }
        } catch (JSONException e) {
            throw new RepositoryException(e);
        }
        return results;
    }

    private String getAccountId() throws RepositoryException, JSONException {
        if (accountId == null) {
            accountId = queryTMDB("/3/account", "session_id", getSessionId()).getString("id");
        }
        return accountId;
    }


    private String getSessionId() throws RepositoryException, JSONException {
        if (token != null && sessionId == null) {
            JSONObject session = queryTMDB("/3/authentication/session/new", "request_token", token);
            sessionId = session.getString("session_id");
        }
        if (sessionId == null) {
            throw new RepositoryException("No open session");
        }
        return sessionId;
    }

    public String createToken() throws RepositoryException, JSONException {
        token = queryTMDB("/3/authentication/token/new").getString("request_token");
        return token;
    }

}