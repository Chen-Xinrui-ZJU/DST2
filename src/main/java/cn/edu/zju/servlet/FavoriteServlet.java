package cn.edu.zju.servlet;

import cn.edu.zju.bean.Drug;
import cn.edu.zju.dao.FavoriteDao;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "FavoriteServlet", urlPatterns = {"/favorites", "/favorites/add", "/favorites/remove"})
public class FavoriteServlet extends HttpServlet {

    private static final int TEST_USER_ID = 1;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        FavoriteDao favoriteDao = new FavoriteDao();
        List<Drug> favoriteDrugs = favoriteDao.findFavoriteDrugsByUserId(TEST_USER_ID);

        request.setAttribute("favoriteDrugs", favoriteDrugs);
        request.getRequestDispatcher("/views/favorites.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String path = request.getServletPath();
        String resourceType = request.getParameter("resourceType");
        String resourceId = request.getParameter("resourceId");

        FavoriteDao favoriteDao = new FavoriteDao();

        if ("/favorites/add".equals(path)) {
            favoriteDao.addFavorite(TEST_USER_ID, resourceType, resourceId);
        } else if ("/favorites/remove".equals(path)) {
            favoriteDao.removeFavorite(TEST_USER_ID, resourceType, resourceId);
        }

        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"success\":true}");
    }
}