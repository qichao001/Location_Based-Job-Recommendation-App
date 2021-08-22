package com.laioffer.job.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laioffer.job.db.MySQLConnection;
import com.laioffer.job.entity.HistoryRequestBody;
import com.laioffer.job.entity.Item;
import com.laioffer.job.entity.ResultResponse;
import com.mysql.cj.x.protobuf.MysqlxSql;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Set;

@WebServlet(name = "HistoryServlet", urlPatterns = {"/history"})
public class HistoryServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(403);
            mapper.writeValue(response.getWriter(), new ResultResponse("Session Invalid"));
            return;
        }

        HistoryRequestBody body = mapper.readValue(request.getReader(), HistoryRequestBody.class);

        MySQLConnection connection = new MySQLConnection();
        connection.setFavoriteItems(body.userId, body.favorite);
        connection.close();

        response.setContentType("application/json");
        ResultResponse resultResponse = new ResultResponse("SUCCESS");
        mapper.writeValue(response.getWriter(), resultResponse);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.setStatus(403);
            mapper.writeValue(resp.getWriter(), new ResultResponse("Session Invalid"));
            return;
        }

        HistoryRequestBody body = mapper.readValue(req.getReader(), HistoryRequestBody.class);
        MySQLConnection connection = new MySQLConnection();
        connection.unsetFavoriteItems(body.userId, body.favorite.getId());
        connection.close();

        resp.setContentType("application/json");
        ResultResponse resultResponse = new ResultResponse("SUCCESS");
        mapper.writeValue(resp.getWriter(), resultResponse);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        ObjectMapper mapper = new ObjectMapper();
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(403);
            mapper.writeValue(response.getWriter(), new ResultResponse("Session Invalid"));
            return;
        }

        String userId = request.getParameter("user_id");

        MySQLConnection connection = new MySQLConnection();
        Set<Item> items = connection.getFavoriteItems(userId);
        connection.close();
        mapper.writeValue(response.getWriter(), items);

    }
}
