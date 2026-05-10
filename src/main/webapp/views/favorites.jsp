<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %>

<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <title>Favorites</title>

    <link href="<%=request.getContextPath()%>/static/bootstrap/css/bootstrap.css" rel="stylesheet">
    <script src="<%=request.getContextPath()%>/static/jquery/jquery-3.4.1.js"></script>
    <script src="<%=request.getContextPath()%>/static/bootstrap/js/bootstrap.bundle.min.js"></script>
    <link href="<%=request.getContextPath()%>/static/css/app.css" rel="stylesheet">

    <style>
        .drug-url-col {
            max-width: 420px;
            word-break: break-all;
        }

        .action-col {
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
            <jsp:param name="active" value="favorites" />
        </jsp:include>

        <main role="main" class="col-md-9 ml-sm-auto col-lg-10 px-4">

            <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
                <h2>Favorite Drugs</h2>
            </div>

            <div class="mb-3 text-muted">
                This page lists drugs that have been marked as favorites by the current user.
            </div>

            <div class="table-responsive">
                <table class="table table-striped table-sm">
                    <thead>
                    <tr>
                        <th>#</th>
                        <th>Name</th>
                        <th class="drug-url-col">Drug Url</th>
                        <th>Biomarker</th>
                        <th class="action-col">Action</th>
                    </tr>
                    </thead>

                    <tbody>
                    <c:choose>
                        <c:when test="${empty favoriteDrugs}">
                            <tr>
                                <td colspan="5" class="text-center empty-text">
                                    No favorite drugs yet.
                                </td>
                            </tr>
                        </c:when>

                        <c:otherwise>
                            <c:forEach items="${favoriteDrugs}" var="item">
                                <tr>
                                    <td>${item.id}</td>
                                    <td>${item.name}</td>
                                    <td class="drug-url-col">
                                        <a href="${item.drugUrl}" target="_blank">
                                                ${item.drugUrl}
                                        </a>
                                    </td>
                                    <td>${item.biomarker}</td>
                                    <td class="action-col">
                                        <form method="post"
                                              action="<%=request.getContextPath()%>/favorites"
                                              style="display:inline;">
                                            <input type="hidden" name="drugId" value="${item.id}">
                                            <input type="hidden" name="action" value="remove">
                                            <input type="hidden" name="redirect" value="favorites">

                                            <button type="submit" class="btn btn-sm btn-warning">
                                                Remove
                                            </button>
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