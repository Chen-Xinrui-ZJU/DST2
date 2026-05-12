package cn.edu.zju.controller;

import cn.edu.zju.bean.DosingGuideline;
import cn.edu.zju.bean.Drug;
import cn.edu.zju.bean.DrugLabel;
import cn.edu.zju.dao.DosingGuidelineDao;
import cn.edu.zju.dao.DrugDao;
import cn.edu.zju.dao.DrugLabelDao;
import cn.edu.zju.dao.FavoriteDao;
import cn.edu.zju.servlet.DispatchServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Set;

public class KnowledgeBaseController {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeBaseController.class);
    private static final String DEFAULT_USER_ID = "zju";

    private DrugDao drugDao = new DrugDao();
    private DrugLabelDao drugLabelDao = new DrugLabelDao();
    private DosingGuidelineDao dosingGuidelineDao = new DosingGuidelineDao();
    private FavoriteDao favoriteDao = new FavoriteDao();

    public void register(DispatchServlet.Dispatcher dispatcher) {
        dispatcher.registerGetMapping("/drugs", this::drugs);
        dispatcher.registerGetMapping("/drugLabels", this::drugLabels);
        dispatcher.registerGetMapping("/dosingGuideline", this::dosingGuideline);
        dispatcher.registerGetMapping("/favorites", this::favorites);
        dispatcher.registerPostMapping("/favorites", this::updateFavorite);
    }

    public void drugs(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String keyword = request.getParameter("keyword");
        String filter = request.getParameter("filter");

        if (isBlank(filter)) {
            filter = "all";
        }

        String userId = getCurrentUserId(request);
        Set<String> favoriteIds = favoriteDao.findFavoriteResourceIds(userId, "drug");

        List<Drug> drugs;

        if (isBlank(keyword)) {
            drugs = drugDao.findAllWithFavoriteIds(favoriteIds);
            request.setAttribute("keyword", "");
        } else {
            keyword = keyword.trim();
            drugs = drugDao.findByKeywordWithFilterAndFavoriteIds(keyword, filter, favoriteIds);
            request.setAttribute("keyword", keyword);
        }

        request.setAttribute("filter", filter);
        request.setAttribute("drugs", drugs);
        request.getRequestDispatcher("/views/drugs.jsp").forward(request, response);
    }

    public void drugLabels(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String keyword = request.getParameter("keyword");
        String filter = request.getParameter("filter");

        if (isBlank(filter)) {
            filter = "all";
        }

        String userId = getCurrentUserId(request);
        Set<String> favoriteIds = favoriteDao.findFavoriteResourceIds(userId, "drug_label");

        List<DrugLabel> drugLabels;

        if (isBlank(keyword)) {
            drugLabels = drugLabelDao.findAllWithFavoriteIds(favoriteIds);
            request.setAttribute("keyword", "");
        } else {
            keyword = keyword.trim();
            drugLabels = drugLabelDao.findByKeywordWithFilterAndFavoriteIds(keyword, filter, favoriteIds);
            request.setAttribute("keyword", keyword);
        }

        request.setAttribute("filter", filter);
        request.setAttribute("drugLabels", drugLabels);
        request.getRequestDispatcher("/views/drug_labels.jsp").forward(request, response);
    }

    public void dosingGuideline(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<DosingGuideline> dosingGuidelines = dosingGuidelineDao.findAll();
        request.setAttribute("dosingGuidelines", dosingGuidelines);
        request.getRequestDispatcher("/views/dosing_guideline.jsp").forward(request, response);
    }

    public void favorites(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String userId = getCurrentUserId(request);

        List<Drug> favoriteDrugs = drugDao.findFavoriteDrugs(userId);
        List<DrugLabel> favoriteDrugLabels = drugLabelDao.findFavoriteDrugLabels(userId);

        request.setAttribute("favoriteDrugs", favoriteDrugs);
        request.setAttribute("favoriteDrugLabels", favoriteDrugLabels);

        request.getRequestDispatcher("/views/favorites.jsp").forward(request, response);
    }

    public void updateFavorite(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String userId = getCurrentUserId(request);

        String resourceType = request.getParameter("resourceType");
        String resourceId = request.getParameter("resourceId");

        /*
         * Compatibility with the old Drugs page.
         * The old form used drugId instead of resourceId.
         */
        String oldDrugId = request.getParameter("drugId");
        if (isBlank(resourceId) && !isBlank(oldDrugId)) {
            resourceId = oldDrugId;
            resourceType = "drug";
        }

        if (isBlank(resourceType)) {
            resourceType = "drug";
        }

        String action = request.getParameter("action");
        String keyword = request.getParameter("keyword");
        String filter = request.getParameter("filter");
        String redirect = request.getParameter("redirect");

        if (isBlank(filter)) {
            filter = "all";
        }

        if (!isBlank(resourceId)) {
            if ("remove".equals(action)) {
                favoriteDao.removeFavorite(userId, resourceType, resourceId);
            } else {
                favoriteDao.addFavorite(userId, resourceType, resourceId);
            }
        }

        if ("favorites".equals(redirect)) {
            response.sendRedirect(request.getContextPath() + "/favorites");
            return;
        }

        String targetPage = "drug_label".equals(resourceType) ? "/drugLabels" : "/drugs";
        StringBuilder redirectUrl = new StringBuilder(request.getContextPath() + targetPage);

        if (!isBlank(keyword)) {
            redirectUrl.append("?keyword=").append(URLEncoder.encode(keyword, "UTF-8"));
            redirectUrl.append("&filter=").append(URLEncoder.encode(filter, "UTF-8"));
        } else if (!"all".equals(filter)) {
            redirectUrl.append("?filter=").append(URLEncoder.encode(filter, "UTF-8"));
        }

        response.sendRedirect(redirectUrl.toString());
    }

    private String getCurrentUserId(HttpServletRequest request) {
        Object userId = request.getSession().getAttribute("userId");
        if (userId != null && !isBlank(String.valueOf(userId))) {
            return String.valueOf(userId);
        }

        Object username = request.getSession().getAttribute("username");
        if (username != null && !isBlank(String.valueOf(username))) {
            return String.valueOf(username);
        }

        return DEFAULT_USER_ID;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}