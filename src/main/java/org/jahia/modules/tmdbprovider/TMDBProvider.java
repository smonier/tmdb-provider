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
import org.jahia.services.cache.ehcache.EhCacheProvider;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.jcr.Binary;
import javax.jcr.ItemNotFoundException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.util.*;

public class TMDBProvider implements ExternalDataSource, ExternalDataSource.LazyProperty {
    private static String API_URL = "api.themoviedb.org";
    private static String API_CONFIGURATION = "/3/configuration";
    private static String API_MOVIE = "/3/movie/";
    private static String API_TV = "/3/tv/";
    private static String API_DISCOVER_MOVIE = "/3/discover/movie";
    private static String API_DISCOVER_TV = "/3/discover/tv";
    private static String API_KEY = "api_key";
    private static String API_KEY_VALUE = "9365395dd91e8a0b7f829d05fc10b54d";

    private EhCacheProvider ehCacheProvider;
    private Ehcache cache;

    private HttpClient httpClient;

    public TMDBProvider() {
        httpClient = new HttpClient();
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void setCacheProvider(EhCacheProvider ehCacheProvider) {
        this.ehCacheProvider = ehCacheProvider;
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
            switch (splitPath.length) {
                case 0:
                    r.add("movies");
                    r.add("tv");
                    break;
                case 2:
                    for (int i = 1900; i <= 2013; i++) {
                        r.add(Integer.toString(i));
                    }
                    break;
                case 3:
                    for (int i = 1; i <= 12; i++) {
                        r.add(StringUtils.leftPad(Integer.toString(i), 2, "0"));
                    }
                    break;
                case 4:
                    if (path.startsWith("/movies")) {
                        JSONObject o = queryTMDB(API_DISCOVER_MOVIE, "release_date.gte", splitPath[2] + "-" + splitPath[3] + "-01", "release_date.lte", splitPath[2] + "-" + splitPath[3] + "-31");
                        JSONArray result = o.getJSONArray("results");
                        for (int i = 0; i < result.length(); i++) {
                            JSONObject movie = result.getJSONObject(i);
                            r.add(movie.getString("id"));
                            cache.put(new Element("movie-" + movie.getString("id"), movie.toString()));
                        }
                    }
                    break;
                case 5:
                    break;

            }
        } catch (JSONException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return r;
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
                return new ExternalData(identifier, "/", "jnt:contentList", new HashMap<String, String[]>());
            }
            if (identifier.contains("-rootfolder")) {
                return new ExternalData(identifier, "/" + StringUtils.substringBefore(identifier, "-rootfolder"), "jnt:contentList", new HashMap<String, String[]>());
            } else if (identifier.contains("-folder-")) {
                return new ExternalData(identifier, "/" + StringUtils.substringBefore(identifier, "-folder-") + "/" + StringUtils.substringAfter(identifier, "-folder-"), "jnt:contentList", new HashMap<String, String[]>());
            } else if (identifier.startsWith("movies-")) {
                String movieId = StringUtils.substringAfter(identifier, "movies-");

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

                String path = "/movies/" + StringUtils.substringBeforeLast(movie.getString("release_date"), "-").replace("-", "/") + "/" + movie.getString("id");

                JSONObject configuration = getConfiguration();
                String baseUrl = configuration.getJSONObject("images").getString("base_url");
                String size = "original";
                Map<String, String[]> properties = new HashMap<String, String[]>();
                properties.put("backdrop_path", new String[]{baseUrl + size + movie.getString("backdrop_path")});
                properties.put("poster_path", new String[]{baseUrl + size + movie.getString("poster_path")});
                properties.put("release_date", new String[]{movie.getString("release_date") + "T00:00:00.000+00:00"});
                properties.put("adult", new String[]{movie.getString("adult")});
                properties.put("vote_average", new String[]{movie.getString("vote_average")});
                properties.put("vote_count", new String[]{movie.getString("vote_count")});
                properties.put("popularity", new String[]{movie.getString("popularity")});

                ExternalData data = new ExternalData(identifier, path, "jnt:movie", properties);

                data.setLazyProperties(Sets.newHashSet("original_title", "homepage", "status", "runtime", "imdb_id", "budget", "revenue"));

                Map<String, Set<String>> lazy18 = new HashMap<String, Set<String>>();
                lazy18.put("en", Sets.newHashSet("title", "overview", "tagline"));
                lazy18.put("fr", Sets.newHashSet("title", "overview", "tagline"));
                data.setLazyI18nProperties(lazy18);

                return data;
            } else if (identifier.startsWith("tv-")) {
                queryTMDB(API_MOVIE + StringUtils.substringAfter(identifier, "tv-"));
            }
        } catch (Exception e) {
            throw new ItemNotFoundException(e);
        }

        throw new ItemNotFoundException(identifier);
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
            switch (splitPath.length) {
                case 0:
                    return getItemByIdentifier("root");
                case 2:
                    return getItemByIdentifier(splitPath[1] + "-rootfolder");
                case 3:
                    return getItemByIdentifier(splitPath[1] + "-folder-" + splitPath[2]);
                case 4:
                    return getItemByIdentifier(splitPath[1] + "-folder-" + splitPath[2] + "/" + splitPath[3]);
                case 5:
                    return getItemByIdentifier(splitPath[1] + "-" + splitPath[4]);
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
        return Sets.newHashSet("jnt:contentList", "jnt:movie");
    }

    /**
     * Indicates if this data source has path-like hierarchical external identifiers, e.g. IDs that are using file system paths.
     *
     * @return <code>true</code> if this data source has path-like hierarchical external identifiers, e.g. IDs that are using file system
     *         paths; <code>false</code> otherwise.
     */
    @Override
    public boolean isSupportsHierarchicalIdentifiers() {
        return true;
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
            m.put(API_KEY, API_KEY_VALUE);

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
    public String[] getPropertyValues(ExternalData data, String propertyName) throws PathNotFoundException {
        return getI18nPropertyValues(data, "en", propertyName);
    }

    @Override
    public String[] getI18nPropertyValues(ExternalData data, String lang, String propertyName) throws PathNotFoundException {
        String result;
        try {
            JSONObject movie;
            if (data.getId().startsWith("movies-")) {

                String movieId = StringUtils.substringAfter(data.getId(), "movies-");
                if (cache.get("fullmovie-" + lang + "-" + movieId) != null) {
                    movie = new JSONObject((String) cache.get("fullmovie-" + lang + "-" + movieId).getObjectValue());
                } else {
                    movie = queryTMDB(API_MOVIE + movieId, "language", lang);
                    cache.put(new Element("fullmovie-" + lang + "-" + movieId, movie.toString()));
                }
                result = movie.getString(propertyName);
//                Map<String, String[]> properties = data.getProperties();
//                properties.put("original_title", new String[]{movie.getString("original_title")});
//                properties.put("homepage", new String[]{movie.getString("homepage")});
//                properties.put("status", new String[]{movie.getString("status")});
//                properties.put("runtime", new String[]{movie.getString("runtime")});
//                properties.put("imdb_id", new String[]{movie.getString("imdb_id")});
//                properties.put("budget", new String[]{movie.getString("budget")});
//                properties.put("revenue", new String[]{movie.getString("revenue")});
//                data.getLazyProperties().clear();
//
//                Map<String, Map<String, String[]>> i18nProperties = new HashMap<String, Map<String, String[]>>();
//                data.setI18nProperties(i18nProperties);
//                i18nProperties.put(lang, new HashMap<String, String[]>());
//                i18nProperties.get(lang).put("title", new String[]{movie.getString("title")});
//                i18nProperties.get(lang).put("overview", new String[]{movie.getString("overview")});
//                i18nProperties.get(lang).put("tagline", new String[]{movie.getString("tagline")});
//                data.setI18nProperties(i18nProperties);
//                data.getLazyI18nProperties().clear();
                return new String[]{result};
            }
        } catch (JSONException e) {
            throw new PathNotFoundException(e);
        } catch (RepositoryException e) {
            throw new PathNotFoundException(e);
        }
        return new String[0];
    }

    @Override
    public Binary[] getBinaryPropertyValues(ExternalData data, String propertyName) throws PathNotFoundException {
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
}