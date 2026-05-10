<%--
  Created by IntelliJ IDEA.
  User: Black Chen
  Date: 2026/5/10
  Time: 14:48
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %>

<jsp:include page="nav.jsp">
    <jsp:param name="active" value="favorites" />
</jsp:include>
<h2>Favorite Drugs</h2>

<c:choose>
    <c:when test="${empty favoriteDrugs}">
        <p>No favorite drugs yet.</p>
    </c:when>
    <c:otherwise>
        <table class="table table-striped table-sm">
            <thead>
            <tr>
                <th>Name</th>
                <th>Drug URL</th>
                <th>Biomarker</th>
                <th>Action</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${favoriteDrugs}" var="item">
                <tr data-drug-id="${item.id}">
                    <td>${item.name}</td>
                    <td>
                        <a href="${item.drugUrl}" target="_blank">${item.drugUrl}</a>
                    </td>
                    <td>${item.biomarker}</td>
                    <td>
                        <button type="button"
                                class="remove-favorite-btn"
                                data-resource-type="drug"
                                data-resource-id="${item.id}">
                            Remove
                        </button>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </c:otherwise>
</c:choose>

<script>
    document.addEventListener("DOMContentLoaded", function () {
        var buttons = document.querySelectorAll(".remove-favorite-btn");

        for (var i = 0; i < buttons.length; i++) {
            buttons[i].addEventListener("click", function () {
                var btn = this;
                var resourceType = btn.getAttribute("data-resource-type");
                var resourceId = btn.getAttribute("data-resource-id");

                var params = "resourceType=" + encodeURIComponent(resourceType)
                    + "&resourceId=" + encodeURIComponent(resourceId);

                fetch("${pageContext.request.contextPath}/favorites/remove", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded"
                    },
                    body: params
                })
                    .then(function (response) {
                        return response.json();
                    })
                    .then(function (data) {
                        if (data.success) {
                            var row = btn.closest("tr");
                            if (row) {
                                row.parentNode.removeChild(row);
                            }
                        }
                    });
            });
        }
    });
</script>
