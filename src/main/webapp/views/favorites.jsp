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

        .summary-col {
            min-width: 280px;
            max-width: 420px;
            white-space: normal;
        }

        .warning-col {
            min-width: 320px;
            max-width: 500px;
            white-space: normal;
        }

        .alternative-col {
            min-width: 280px;
            max-width: 420px;
            white-space: normal;
        }

        .action-col {
            min-width: 130px;
            text-align: center;
        }

        .empty-text {
            color: #888;
        }

        .section-title {
            margin-top: 24px;
            margin-bottom: 12px;
        }

        .drug-label-table {
            min-width: 1500px;
            table-layout: auto;
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
                <h2>Favorites</h2>
            </div>

            <h3 class="section-title">Favorite Drugs</h3>

            <div class="mb-3 text-muted">
                This section lists drugs that have been marked as favorites by the current user.
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
                                            <input type="hidden" name="resourceType" value="drug">
                                            <input type="hidden" name="resourceId" value="${item.id}">
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

            <h3 class="section-title">Favorite Drug Labels</h3>

            <div class="mb-3 text-muted">
                This section lists drug labels that have been marked as favorites by the current user.
            </div>

            <div class="table-responsive">
                <table class="table table-striped table-sm drug-label-table">
                    <thead>
                    <tr>
                        <th>#</th>
                        <th>Source</th>
                        <th>Dosing Information</th>
                        <th class="summary-col">Efficacy Summary</th>
                        <th class="warning-col">Response Warning</th>
                        <th class="alternative-col">Alternative Drug</th>
                        <th class="action-col">Action</th>
                    </tr>
                    </thead>

                    <tbody>
                    <c:choose>
                        <c:when test="${empty favoriteDrugLabels}">
                            <tr>
                                <td colspan="7" class="text-center empty-text">
                                    No favorite drug labels yet.
                                </td>
                            </tr>
                        </c:when>

                        <c:otherwise>
                            <c:forEach items="${favoriteDrugLabels}" var="item">
                                <tr>
                                    <td>${item.id}</td>
                                    <td>${item.source}</td>
                                    <td>${item.dosingInformation}</td>
                                    <td class="summary-col">${item.efficacySummary}</td>
                                    <td class="warning-col">${item.responseWarning}</td>
                                    <td class="alternative-col">${item.alternativeDrug}</td>
                                    <td class="action-col">
                                        <form method="post"
                                              action="<%=request.getContextPath()%>/favorites"
                                              style="display:inline;">
                                            <input type="hidden" name="resourceType" value="drug_label">
                                            <input type="hidden" name="resourceId" value="${item.id}">
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