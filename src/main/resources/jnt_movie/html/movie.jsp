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

<c:choose>
    <%-- If no image has been supplied for the image view, put a placeholder image in place --%>
    <c:when test="${empty image}">
        <c:url var="imageUrl" value="${url.currentModule}/img/background.jpg"/>
    </c:when>
    <c:otherwise>
        <c:url var="imageUrl" value="${image}"/>
    </c:otherwise>
</c:choose>

<%-- check if the link property has been used on this content --%>

    <c:set var="linkUrl" value="${url.base}${currentNode.path}.html"/>


    <div class="thumbnails thumbnail-style thumbnail-kenburn">
        <div class="thumbnail-img">
            <div class="overflow-hidden">
                <img class="img-responsive" src="${imageUrl}" alt="">
            </div>
            <%-- only display the read more text if a link has been provided --%>
            <c:if test="${not empty linkUrl}">
                <a class="btn-more hover-effect" href="${linkUrl}" alt="${title}"><fmt:message key="jnt_movie.readmore"/> +</a>
            </c:if>
        </div>
        <div class="caption">
            <c:choose>
            <c:when test="${not empty linkUrl}">
            <h3><a class="hover-effect" href="${linkUrl}">${title}</a></h3>
            </c:when>
                <c:otherwise>
                    <h3><a class="hover-effect" href="#">${title}</a></h3>
                </c:otherwise>
            </c:choose>
            <p>${description}</p>
        </div>
    </div>