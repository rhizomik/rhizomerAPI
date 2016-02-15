package net.rhizomik.rhizomer;

/**
 * Created by http://rhizomik.net/~roberto/
 */
        import org.springframework.beans.factory.annotation.Value;
        import org.springframework.stereotype.Component;

        import javax.servlet.*;
        import javax.servlet.http.HttpServletResponse;
        import java.io.IOException;

@Component
public class CORSFilter implements Filter {

    @Value("${cors.filter.allowedOrigins}")
    String allowedOrigins;

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;
        response.setHeader("Access-Control-Allow-Origin", allowedOrigins);
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "x-requested-with");
        chain.doFilter(req, res);
    }

    public void init(FilterConfig filterConfig) {}

    public void destroy() {}
}
