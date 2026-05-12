package cn.edu.zju.servlet;

import javax.servlet.http.HttpServlet;

/*
 * This servlet is kept only for compatibility with the old project structure.
 * It is no longer mapped to /drugLabels.
 *
 * The /drugLabels route should be handled by KnowledgeBaseController
 * through DispatchServlet, so that keyword/filter search and favorite state
 * can work correctly.
 */
public class DrugLabelServlet extends HttpServlet {
}