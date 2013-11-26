<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

<div id="leftCol" class="well">

    <h3 style="margin-top: 2px;">Posters </h3>
    <a href="${currentNode.properties['poster_path'].string}"><img id="upload_poster" class="shadow" src="${currentNode.properties['poster_path'].string}" width="185" height="278" itemprop="image"></a>

    <h3>Language</h3>



    <h3>Original Title</h3>
    <p>${currentNode.properties['original_title'].string}</p>

    <h3>Movie Facts</h3>

    <p><strong>Status:</strong> <span id="status">${currentNode.properties['status'].string}</span></p>
    <p><strong>Runtime:</strong> <span id="runtime"><meta itemprop="duration" content="PT1M57S">${currentNode.properties['runtime'].string}</span></p>
    <p><strong>Budget:</strong> <span id="budget">${currentNode.properties['budget'].string}</span></p>
    <p><strong>Revenue:</strong> <span id="revenue">${currentNode.properties['revenue'].string}</span></p>
    <p><strong>Language:</strong> <span id="languages">${currentNode.properties['spoken_languages'].string}</span></p>
    <p><strong>Webpage:</strong> <span id="homepage">${currentNode.properties['homepage'].string}</span></p>

    <h3>Release Info</h3>
    <ul id="release_date_list" class="certification hide">
        <li>
            <%--<img src="http://d3a8mw37cqal2z.cloudfront.net/assets/a25e6ad4f410e17/images/flags/jp.png">--%>
            <%--<p><span itemprop="datePublished">1984-03-11</span> <img style="margin: -1px 0 0 0;" src="http://d3a8mw37cqal2z.cloudfront.net/assets/fbbf6bacaba2848/images/tick-small.png" width="16" height="16"><br>&nbsp;-</p>--%>
        </li>
    </ul>

    <h3>Trailers</h3>
    <p class="trailers">No <img src="http://d3a8mw37cqal2z.cloudfront.net/assets/2862726013ad06d/images/youtube-logo.png" width="15" height="16" alt="YouTube"> trailers found.</p>

    <h3>Plot Keywords</h3>

    <ul class="keywords">
        <%--<li><a href="/keyword/30-individual"><span itemprop="keywords">individual</span></a></li>--%>
        <%--<li><a href="/keyword/83-saving-the-world"><span itemprop="keywords">saving the world</span></a></li>--%>
        <%--<li><a href="/keyword/333-mushroom"><span itemprop="keywords">mushroom</span></a></li>--%>
        <%--<li><a href="/keyword/334-flying"><span itemprop="keywords">flying</span></a></li>--%>
        <%--<li><a href="/keyword/335-gaia-hypothesis"><span itemprop="keywords">gaia hypothesis</span></a></li>--%>
        <%--<li><a href="/keyword/3800-airplane"><span itemprop="keywords">airplane</span></a></li>--%>
        <%--<li><a href="/keyword/1935-wind"><span itemprop="keywords">wind</span></a></li>--%>
        <%--<li><a href="/keyword/2763-human-being-versa-nature"><span itemprop="keywords">human being versa nature</span></a></li>--%>
        <%--<li><a href="/keyword/6091-war"><span itemprop="keywords">war</span></a></li>--%>
        <%--<li><a href="/keyword/2765-fungus-spores"><span itemprop="keywords">fungus spores   </span></a></li>--%>
        <%--<li><a href="/keyword/164007-studio-ghibli"><span itemprop="keywords">studio ghibli</span></a></li>--%>
        <%--<div style="clear: both;"></div>--%>
    </ul>

    <h3>Alternative Titles</h3>
    <ul>
        <%--<li>Kaze no tani no Naushika</li>--%>
        <%--<li>Nausicaä of the Valley of the Wind</li>--%>
        <%--<li>A szél harcosai</li>--%>
        <%--<li>Guerreros del viento</li>--%>
        <%--<li>Nausicaä del Valle del Viento</li>--%>
        <%--<li>I nafsika tis koiladas ton anemon</li>--%>
        <%--<li>Naushika z Údolí větru</li>--%>
        <%--<li>Nausicaä - Aus dem Tal der Winde</li>--%>
        <%--<li>Nausicaä - Prinsessen fra vindens dal</li>--%>
        <%--<li>Nausicaä de la vallée du vent</li>--%>
        <%--<li>Nausicaä della valle del vento</li>--%>
        <%--<li>Nausicaä från vindarnas dal</li>--%>
        <%--<li>Vindens krigare</li>--%>
        <%--<li>Nausicaä z Doliny Wiatru</li>--%>
        <%--<li>Rüzgarli vadi</li>--%>
        <%--<li>Tuulen Laakson Nausicaä</li>--%>
        <%--<li>Tuulte oru Nausikaa</li>--%>
        <%--<li>Warriors of the Wind</li>--%>
        <%--<li>Kaze no Tani no Nausicaä</li>--%>
        <%--<li>Nausicaae Aus dem Tal der Winde</li>--%>
        <%--<li>風之谷</li>--%>
        <%--<li>Nausicaa de la vallee du vent</li>--%>
    </ul>

</div>

