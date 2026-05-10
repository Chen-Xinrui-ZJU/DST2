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
    <title>Drug Labels</title>

    <link href="<%=request.getContextPath()%>/static/bootstrap/css/bootstrap.css" rel="stylesheet">
    <script src="<%=request.getContextPath()%>/static/jquery/jquery-3.4.1.js"></script>
    <script src="<%=request.getContextPath()%>/static/bootstrap/js/bootstrap.bundle.min.js"></script>
    <link href="<%=request.getContextPath()%>/static/css/app.css" rel="stylesheet">

    <style>
        .table-responsive {
            overflow-x: auto;
            width: 100%;
        }

        .drug-label-table {
            min-width: 1900px;
            table-layout: auto;
        }

        .drug-label-table th,
        .drug-label-table td {
            vertical-align: top;
        }

        .summary-col {
            min-width: 320px;
            max-width: 420px;
        }

        .warning-col {
            min-width: 380px;
            max-width: 520px;
        }

        .alternative-col {
            min-width: 320px;
            max-width: 450px;
            white-space: normal;
            text-align: left;
        }

        .search-card {
            background: #f8f9fa;
            border: 1px solid #e5e5e5;
            border-radius: 6px;
            padding: 16px;
            margin-bottom: 18px;
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
            <jsp:param name="active" value="drug_labels" />
        </jsp:include>

        <main role="main" class="col-md-9 ml-sm-auto col-lg-10 px-4">

            <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
                <h2>Drug Labels</h2>
            </div>

            <div class="search-card">
                <form method="get" action="<%=request.getContextPath()%>/drugLabels">
                    <div class="form-row align-items-center">

                        <div class="col-md-3 mb-2">
                            <select class="form-control" name="filter">
                                <option value="all" ${filter == 'all' ? 'selected' : ''}>All fields</option>
                                <option value="id" ${filter == 'id' ? 'selected' : ''}>Label ID</option>
                                <option value="name" ${filter == 'name' ? 'selected' : ''}>Label Name</option>
                                <option value="source" ${filter == 'source' ? 'selected' : ''}>Source</option>
                                <option value="drug_id" ${filter == 'drug_id' ? 'selected' : ''}>Drug ID</option>
                                <option value="dosing_information" ${filter == 'dosing_information' ? 'selected' : ''}>Dosing Information</option>
                                <option value="summary_markdown" ${filter == 'summary_markdown' ? 'selected' : ''}>Summary Markdown</option>
                                <option value="efficacy_summary" ${filter == 'efficacy_summary' ? 'selected' : ''}>Efficacy Summary</option>
                                <option value="response_warning" ${filter == 'response_warning' ? 'selected' : ''}>Response Warning</option>
                                <option value="alternative_drug" ${filter == 'alternative_drug' ? 'selected' : ''}>Alternative Drug</option>
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
                               href="<%=request.getContextPath()%>/drugLabels">
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
                <table class="table table-striped table-sm drug-label-table">
                    <thead>
                    <tr>
                        <th>#</th>
                        <th>Source</th>
                        <th>Dosing Information</th>
                        <th class="summary-col">Summary Markdown</th>
                        <th class="summary-col">Efficacy Summary</th>
                        <th class="warning-col">Response Warning</th>
                        <th class="alternative-col">Alternative Drug</th>
                    </tr>
                    </thead>

                    <tbody>
                    <c:choose>
                        <c:when test="${empty drugLabels}">
                            <tr>
                                <td colspan="7" class="text-center empty-text">
                                    No drug labels found.
                                </td>
                            </tr>
                        </c:when>

                        <c:otherwise>
                            <c:forEach items="${drugLabels}" var="item">
                                <tr>
                                    <td>${item.id}</td>
                                    <td>${item.source}</td>
                                    <td>${item.dosingInformation}</td>
                                    <td class="summary-col">${item.summaryMarkdown}</td>
                                    <td class="summary-col">${item.efficacySummary}</td>
                                    <td class="warning-col">${item.responseWarning}</td>
                                    <td class="alternative-col">
                                        <c:choose>
                                            <c:when test="${not empty item.alternativeDrug}">
                                                ${item.alternativeDrug}
                                            </c:when>
                                            <c:otherwise>
                                                -
                                            </c:otherwise>
                                        </c:choose>
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