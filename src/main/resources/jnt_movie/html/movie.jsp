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


<c:set var="title" value="${currentNode.properties['original_title'].string}"/>
<c:set var="description" value="${currentNode.properties['overview'].string}"/>
<c:set var="image" value="${currentNode.properties['backdrop_path'].string}"/>
<c:set var="linkUrl" value="${url.base}${currentNode.path}.html"/>
<c:set var="vote_average" value="${currentNode.properties['vote_average'].string}"/>
<c:set var="release_date" value="${currentNode.properties['release_date']}"/>
<c:choose>
    <%-- If no image has been supplied for the image view, put a placeholder image in place --%>
    <c:when test="${empty image}">
        <c:url var="imageUrl" value="${url.currentModule}/img/background.jpg"/>
    </c:when>
    <c:otherwise>
        <c:url var="imageUrl" value="${image}"/>
    </c:otherwise>
</c:choose>
<div class="card mb-3">
    <img class="card-img-top" src="${imageUrl}" alt="Card image cap">
    <div class="card-body">
        <h5 class="card-title">${title}</h5>
        <p class="card-text">${description}</p>
        <p class="card-text">Release date: <fmt:formatDate value="${release_date.date.time}" pattern="dd/MM/yyyy"/>
        <div class="text-right">vote: ${vote_average}</div>
        </p>
        <p class="card-text"><small class="text-muted">
            <c:if test="${not empty linkUrl}">
                <a class="btn btn-dark hover-effect" href="${linkUrl}" alt="${title}"><fmt:message
                        key="jnt_movie.readmore"/> +</a>
            </c:if></small>
        </p>
    </div>
</div>

