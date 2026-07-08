package com.smart.parking.web;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import java.io.IOException;

@Controller
public class PortalController {

    // ── Clean portal entry URLs ─────────────────────────────────────────────
    @GetMapping("/customer")
    public void customerPortal(HttpServletResponse r) throws IOException {
        r.sendRedirect("/customer/login.html");
    }

    @GetMapping("/admin")
    public void adminPortal(HttpServletResponse r) throws IOException {
        r.sendRedirect("/admin/login.html");
    }

    @GetMapping("/manager")
    public void managerPortal(HttpServletResponse r) throws IOException {
        r.sendRedirect("/manager/login.html");
    }

    @GetMapping("/staff")
    public void staffPortal(HttpServletResponse r) throws IOException {
        r.sendRedirect("/staff/login.html");
    }

    @GetMapping("/super")
    public void superPortal(HttpServletResponse r) throws IOException {
        r.sendRedirect("/super/login.html");
    }

    @GetMapping("/state")
    public void statePortal(HttpServletResponse r) throws IOException {
        r.sendRedirect("/state/login.html");
    }

    // ── Fix favicon 500 ─────────────────────────────────────────────────────
    @GetMapping("/favicon.ico")
    @ResponseBody
    public void favicon(HttpServletResponse r) {
        r.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
