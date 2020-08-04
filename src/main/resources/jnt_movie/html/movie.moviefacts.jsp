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


<div class="card mb-3  border-0 ">
    <img class="card-img-top imgPoster" src="${currentNode.properties['poster_path'].string}" alt="Card image cap">
    <div class="card-body">
        <h3 class="card-title">${currentNode.properties['original_title'].string}</h3>
        <p class="card-text"> <h4>Movie Facts</h4></p>
        <p class="card-text">

        <p><strong>Status:</strong> <span id="status">${currentNode.properties['status'].string}</span></p>
        <p><strong>Runtime:</strong> <span id="runtime"><meta itemprop="duration" content="PT1M57S">
            ${currentNode.properties['runtime'].string}</span> min
        </p>
        <fmt:setLocale value="en_US"/>
        <p><strong>Budget:</strong> <span id="budget"><fmt:formatNumber
                value="${currentNode.properties['budget'].string}" type="currency" currencySymbol="$"/></span></p>
        <p><strong>Revenue:</strong> <span id="revenue"><fmt:formatNumber
                value="${currentNode.properties['revenue'].string}" type="currency" currencySymbol="$"/></span></p>
        <p><strong>Webpage:</strong> <span id="homepage"><a
                href="${currentNode.properties['homepage'].string}">${currentNode.properties['homepage'].string}</a></span>
        </p>
        </p>


        <p>
        <hr>
        <h5>Genres</h5>
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
                            document.write('<span class="tag">' + obj["name"] + '</span><br/>');
                        }
                    }
                    ;
                }
            };

            // Printing all the values from the resulting object
            printValues(obj);
        </script>
        </p>
        <div class="">
            <hr>
            <h5>Popularity</h5>
            <fmt:formatNumber var="popularity" type="number" minFractionDigits="0" maxFractionDigits="0"
                              value="${currentNode.properties['vote_average'].string*10 } "/>
            <p>
            <div class="c100 p${popularity} small">
                <span>${popularity}%</span>
                <div class="slice">
                    <div class="bar"></div>
                    <div class="fill"></div>
                </div>
            </div>
            </p>
        </div>
    </div>
</div>


