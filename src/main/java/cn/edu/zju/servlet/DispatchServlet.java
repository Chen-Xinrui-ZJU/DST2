package cn.edu.zju.servlet;

import cn.edu.zju.controller.IndexController;
import cn.edu.zju.controller.KnowledgeBaseController;
import cn.edu.zju.controller.MatchingController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class DispatchServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(DispatchServlet.class);

    @FunctionalInterface
    public interface HttpConsumer<T, U> {
        void accept(T t, U u) throws Exception;
    }

    private ConcurrentHashMap<String, HttpConsumer<HttpServletRequest, HttpServletResponse>> getRequestMapping;
    private ConcurrentHashMap<String, HttpConsumer<HttpServletRequest, HttpServletResponse>> postRequestMapping;

    private final HttpConsumer<HttpServletRequest, HttpServletResponse> notFound = (request, response) -> {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.getWriter().write("Not Found");
    };

    public class Dispatcher {

        public void registerGetMapping(String path,
                                       HttpConsumer<HttpServletRequest, HttpServletResponse> consumer) {
            getRequestMapping.put(path, consumer);
        }

        public void registerPostMapping(String path,
                                        HttpConsumer<HttpServletRequest, HttpServletResponse> consumer) {
            postRequestMapping.put(path, consumer);
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        this.getRequestMapping = new ConcurrentHashMap<>();
        this.postRequestMapping = new ConcurrentHashMap<>();

        Dispatcher dispatcher = new Dispatcher();

        IndexController indexController = new IndexController();
        indexController.register(dispatcher);

        KnowledgeBaseController knowledgeBaseController = new KnowledgeBaseController();
        knowledgeBaseController.register(dispatcher);

        MatchingController matchingController = new MatchingController();
        matchingController.register(dispatcher);

        log.info("DispatchServlet initialized.");
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = getRequestPath(req);
        log.info("{}: {}", req.getMethod(), path);
        super.service(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = getRequestPath(req);
        HttpConsumer<HttpServletRequest, HttpServletResponse> consumer =
                getRequestMapping.getOrDefault(path, notFound);

        try {
            consumer.accept(req, resp);
        } catch (Exception e) {
            log.info("", e);
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = getRequestPath(req);
        HttpConsumer<HttpServletRequest, HttpServletResponse> consumer =
                postRequestMapping.getOrDefault(path, notFound);

        try {
            consumer.accept(req, resp);
        } catch (Exception e) {
            log.info("", e);
            throw new ServletException(e);
        }
    }

    private String getRequestPath(HttpServletRequest req) {
        String servletPath = req.getServletPath();
        String pathInfo = req.getPathInfo();

        if (servletPath != null && !servletPath.trim().isEmpty() && !"/".equals(servletPath)) {
            return servletPath;
        }

        if (pathInfo != null && !pathInfo.trim().isEmpty()) {
            return pathInfo;
        }

        return "/";
    }
}