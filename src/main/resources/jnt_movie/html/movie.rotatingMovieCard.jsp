<%@ page language="java" contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="ui" uri="http://www.jahia.org/tags/uiComponentsLib" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>
<%@ taglib prefix="query" uri="http://www.jahia.org/tags/queryLib" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="s" uri="http://www.jahia.org/tags/search" %>
<%--@elvariable id="currentNode" type="org.jahia.services.content.JCRNodeWrapper"--%>
<%--@elvariable id="out" type="java.io.PrintWriter"--%>
<%--@elvariable id="script" type="org.jahia.services.render.scripting.Script"--%>
<%--@elvariable id="scriptInfo" type="java.lang.String"--%>
<%--@elvariable id="workspace" type="java.lang.String"--%>
<%--@elvariable id="renderContext" type="org.jahia.services.render.RenderContext"--%>
<%--@elvariable id="currentResource" type="org.jahia.services.render.Resource"--%>
<%--@elvariable id="url" type="org.jahia.services.render.URLGenerator"--%>

<template:addResources type="css" resources="tmdbmovie.css"/>
<template:addResources type="css" resources="circle.css"/>
<template:addResources type="css" resources="rotatingCard.scss"/>


<a class="card" href="#!">
    <div class="front" style="background-image: url(${currentNode.properties['poster_path'].string});">
        <p>${currentNode.properties['original_title'].string}</p>
    </div>
    <div class="back">
        <img class="card-img-top" src="${currentNode.properties['backdrop_path'].string}" alt="Card image cap">
        <div class="release_date">${currentNode.properties['original_title'].string}
            (${currentNode.properties['release_date'].date.time.year + 1900})
        </div>
        <script>
            /* Storing multi-line JSON string in a JS variable
            using the new ES6 template literals */
            var json = `${currentNode.properties['genres'].string}`;

            // Converting JSON object to JS object
            var obj = JSON.parse(json);

            // Define recursive function to print nested values
            function printValues(obj) {
                for (var k in obj) {
                    if (obj[k] instanceof Object) {
                        printValues(obj[k]);
                    } else {
                        if (k == "name") {
                            document.write('<span>' + obj["name"] + '</span>, ');
                        }
                    }
                    ;
                }
            };

            // Printing all the values from the resulting object
            printValues(obj);
        </script>
        <div class="popularity">
            <fmt:formatNumber var="popularity" type="number" minFractionDigits="0" maxFractionDigits="0"
                              value="${currentNode.properties['vote_average'].string*10 } "/>

            <div class="c100 p${popularity} small">
                <span>${popularity}%</span>
                <div class="slice">
                    <div class="bar"></div>
                    <div class="fill"></div>
                </div>
            </div>
        </div>
        <div>
            <p>${currentNode.properties['overview'].string}</p>
            <div class="pt-2">
                <a href="https://www.imdb.com/title/${currentNode.properties['imdb_id'].string}"><img
                        src="https://m.media-amazon.com/images/G/01/IMDb/BG_rectangle._CB1509060989_SY230_SX307_AL_.png"
                        width="80px"/></a>
            </div>
        </div>
    </div>
</a>


<!-- end movie-card -->