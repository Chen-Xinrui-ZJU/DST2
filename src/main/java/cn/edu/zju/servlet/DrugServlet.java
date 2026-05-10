package cn.edu.zju.servlet;

import cn.edu.zju.bean.Drug;
import cn.edu.zju.dao.DrugDao;
import cn.edu.zju.dao.FavoriteDao;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Set;

@WebServlet(name = "DrugServlet", urlPatterns = "/drugs")
public class DrugServlet extends HttpServlet {

    private static final String TEST_USER_ID = "zju";

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        DrugDao drugDao = new DrugDao();
        FavoriteDao favoriteDao = new FavoriteDao();

        List<Drug> drugs = drugDao.findAll();
        Set<String> favoriteDrugIds = favoriteDao.findFavoriteResourceIds(TEST_USER_ID, "drug");

        for (Drug drug : drugs) {
            drug.setFavorited(favoriteDrugIds.contains(drug.getId()));
        }

        request.setAttribute("drugs", drugs);
        request.getRequestDispatcher("/views/drugs.jsp").forward(request, response);
    }
}