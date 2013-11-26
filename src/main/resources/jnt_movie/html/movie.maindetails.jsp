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

<div id="mainCol">
<div class="title">
    <h2 id="title"><span itemprop="name">${currentNode.properties['jcr:title'].string}</span> (${currentNode.properties['release_date'].date.time.year + 1900})</h2>
</div>

<div class="rating">
    <div id="updateRating" itemprop="aggregateRating" itemscope="" itemtype="http://schema.org/AggregateRating">
        <p class="average"><span id="rating_hint" itemprop="ratingValue">${currentNode.properties['vote_average'].string}</span>/<span itemprop="bestRating">10</span> (<span itemprop="ratingCount">${currentNode.properties['vote_count'].string}</span> votes)</p>
    </div>
</div>


<h3>Overview </h3>
<p id="overview" class="lead" itemprop="description">${currentNode.properties['overview'].string}</p>

<h3>Tagline</h3>
<p id="tagline">${currentNode.properties['tagline'].string}</p>

<h3>Crew</h3>
<table class="crewStub" border="0" cellspacing="0" cellpadding="0">
    <tbody>
    <tr>
        <td class="job">Director:</td>
        <%--<td class="person"><span itemprop="director" itemscope="" itemtype="http://schema.org/Person"><a href="/person/608-hayao-miyazaki" itemprop="url"><span itemprop="name">Hayao Miyazaki</span></a></span></td>--%>
    </tr>
    <tr>
        <td class="job">Writer:</td>
        <%--<td class="person"><span itemprop="author" itemscope="" itemtype="http://schema.org/Person"><a href="/person/608-hayao-miyazaki" itemprop="url"><span itemprop="name">Hayao Miyazaki</span></a></span></td>--%>
    </tr>
    </tbody>
</table>


<h3>Cast</h3>
<table id="castTable" class="cast" border="0" cellspacing="0" cellpadding="0">
    <tbody>
    <tr>
    </tr>
    </tbody>
</table>


<h3>Backdrops</h3>

<div id="backdrop_window"></div>
<div id="backdrops" class="image_carousel">
    <div class="caroufredsel_wrapper" style="display: block; text-align: start; float: none; position: relative; top: auto; right: auto; bottom: auto; left: auto; z-index: auto; width: 744px; height: 189px; margin: 0px; overflow: hidden;"><div id="images" style="text-align: left; float: none; position: absolute; top: 0px; right: auto; bottom: auto; left: 0px; margin: 0px; width: 3856px; height: 189px;">
    </div></div>

    <div style="clear: both;"></div>
</div>


<div style="clear: both;"></div>
</div>

