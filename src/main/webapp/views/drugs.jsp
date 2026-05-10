<%--
  Created by IntelliJ IDEA.
  User: hello
  Date: 2019-12-3
  Time: 15:37
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %>

<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <title>Drugs</title>

    <link href="<%=request.getContextPath()%>/static/bootstrap/css/bootstrap.css" rel="stylesheet">
    <script src="<%=request.getContextPath()%>/static/jquery/jquery-3.4.1.js"></script>
    <script src="<%=request.getContextPath()%>/static/bootstrap/js/bootstrap.bundle.min.js"></script>
    <link href="<%=request.getContextPath()%>/static/css/app.css" rel="stylesheet">

    <style>
        .search-card {
            background: #f8f9fa;
            border: 1px solid #e5e5e5;
            border-radius: 6px;
            padding: 16px;
            margin-bottom: 18px;
        }

        .drug-url-col {
            max-width: 420px;
            word-break: break-all;
        }

        .favorite-col {
            min-width: 130px;
            text-align: center;
        }

        .empty-text {
            color: #888;
        }
    </style>
</head>

<body>
<jsp:include page="head.jsp" />

<div class="container-fluid">
    <div class="row">
        <jsp:include page="nav.jsp">
            <jsp:param name="active" value="drugs" />
        </jsp:include>

        <main role="main" class="col-md-9 ml-sm-auto col-lg-10 px-4">

            <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
                <h2>Drugs</h2>
            </div>

            <div class="search-card">
                <form method="get" action="<%=request.getContextPath()%>/drugs">
                    <div class="form-row align-items-center">

                        <div class="col-md-3 mb-2">
                            <select class="form-control" name="filter">
                                <option value="all" ${filter == 'all' ? 'selected' : ''}>All fields</option>
                                <option value="id" ${filter == 'id' ? 'selected' : ''}>Drug ID</option>
                                <option value="name" ${filter == 'name' ? 'selected' : ''}>Drug Name</option>
                                <option value="obj_cls" ${filter == 'obj_cls' ? 'selected' : ''}>Object Class</option>
                                <option value="drug_url" ${filter == 'drug_url' ? 'selected' : ''}>Drug URL</option>
                                <option value="biomarker" ${filter == 'biomarker' ? 'selected' : ''}>Biomarker</option>
                            </select>
                        </div>

                        <div class="col-md-5 mb-2">
                            <input type="text"
                                   class="form-control"
                                   name="keyword"
                                   value="${keyword}"
                                   placeholder="Enter keyword...">
                        </div>

                        <div class="col-md-4 mb-2">
                            <button type="submit" class="btn btn-primary">
                                Search
                            </button>

                            <a class="btn btn-outline-secondary ml-2"
                               href="<%=request.getContextPath()%>/drugs">
                                Reset
                            </a>
                        </div>
                    </div>
                </form>

                <c:if test="${not empty keyword}">
                    <div class="mt-2 text-muted">
                        Showing results for:
                        <strong>${keyword}</strong>
                    </div>
                </c:if>
            </div>

            <div class="table-responsive">
                <table class="table table-striped table-sm">
                    <thead>
                    <tr>
                        <th>#</th>
                        <th>Name</th>
                        <th class="drug-url-col">Drug Url</th>
                        <th>Biomarker</th>
                        <th class="favorite-col">Favorite</th>
                    </tr>
                    </thead>

                    <tbody>
                    <c:choose>
                        <c:when test="${empty drugs}">
                            <tr>
                                <td colspan="5" class="text-center empty-text">
                                    No drugs found.
                                </td>
                            </tr>
                        </c:when>

                        <c:otherwise>
                            <c:forEach items="${drugs}" var="item">
                                <tr>
                                    <td>${item.id}</td>
                                    <td>${item.name}</td>
                                    <td class="drug-url-col">
                                        <a href="${item.drugUrl}" target="_blank">
                                                ${item.drugUrl}
                                        </a>
                                    </td>
                                    <td>${item.biomarker}</td>
                                    <td class="favorite-col">
                                        <form method="post"
                                              action="<%=request.getContextPath()%>/favorites"
                                              style="display:inline;">
                                            <input type="hidden" name="drugId" value="${item.id}">
                                            <input type="hidden" name="keyword" value="${keyword}">
                                            <input type="hidden" name="filter" value="${filter}">
                                            <input type="hidden" name="redirect" value="drugs">

                                            <c:choose>
                                                <c:when test="${item.favorited}">
                                                    <input type="hidden" name="action" value="remove">
                                                    <button type="submit" class="btn btn-sm btn-warning">
                                                        Unfavorite
                                                    </button>
                                                </c:when>

                                                <c:otherwise>
                                                    <input type="hidden" name="action" value="add">
                                                    <button type="submit" class="btn btn-sm btn-outline-primary">
                                                        Favorite
                                                    </button>
                                                </c:otherwise>
                                            </c:choose>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                    </tbody>
                </table>
            </div>

        </main>
    </div>
</div>
</body>
</html>