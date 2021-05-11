package me.novascomp.files.download;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.AsyncContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import me.novascomp.files.model.FileShare;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

@WebServlet(urlPatterns = "/byToken", asyncSupported = true)
public class WebDownloadAsyncServlet extends HttpServlet {

    @Autowired
    private DownloadSessionService downloadSessionService;
    private WebApplicationContext springContext;

    private static final Logger LOG = Logger.getLogger(WebDownloadAsyncServlet.class.getName());

    @Override //https://stackoverflow.com/questions/23839226/autowire-null-inside-a-servlet
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);
        springContext = WebApplicationContextUtils.getRequiredWebApplicationContext(config.getServletContext());
        final AutowireCapableBeanFactory beanFactory = springContext.getAutowireCapableBeanFactory();
        beanFactory.autowireBean(this);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {

        LOG.log(Level.INFO, request.getPathInfo());
        String[] pathArray = request.getPathInfo().split("/");
        FileShare fileShare = downloadSessionService.getFileShare(pathArray[pathArray.length - 1]);

        if (fileShare == null) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }

        response.setHeader("Content-disposition", "attachment; filename*=UTF-8''" + URLEncoder.encode(fileShare.getFileId().getName(), java.nio.charset.StandardCharsets.UTF_8).replace("+", " "));
        request.setAttribute("org.apache.catalina.ASYNC_SUPPORTED", true);

        AsyncContext asyncCtx = request.startAsync();
        asyncCtx.addListener(new DownloadListener(downloadSessionService.getDownloadStatus()));
        asyncCtx.setTimeout(36000);

        ThreadPoolExecutor executor = (ThreadPoolExecutor) request.getServletContext().getAttribute("executor");
        try {
            NVDownloadManager downloadManager = new NVDownloadManager(asyncCtx, new URL(fileShare.getLink() + "/" + "download"));
            executor.execute(downloadManager);
        } catch (MalformedURLException ex) {
            Logger.getLogger(WebDownloadAsyncServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
