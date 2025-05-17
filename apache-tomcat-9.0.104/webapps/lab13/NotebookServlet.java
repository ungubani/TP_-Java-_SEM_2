import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class NotebookServlet extends HttpServlet {
    private Notebook notebook;
    private String filePath;

    @Override
    public void init() throws ServletException {
        filePath = getServletContext().getRealPath("/") + "notebook.txt";
        notebook = new Notebook();
        try {
            notebook.loadFromFile(filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected synchronized void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        out.println("<html><head><title>Записная книжка</title></head><body>");
        out.println("<h2>Записная книжка</h2>");

        if ("addUser".equals(action)) {
            out.println("<form method='post'>");
            out.println("Имя: <input type='text' name='username'/>");
            out.println("<input type='hidden' name='action' value='addUser'/>");
            out.println("<input type='submit' value='Добавить'/>");
            out.println("</form>");
        } else if ("addPhone".equals(action)) {
            out.println("<form method='post'>");
            out.println("Имя: <input type='text' name='username'/><br/>");
            out.println("Телефон: <input type='text' name='phone'/>");
            out.println("<input type='hidden' name='action' value='addPhone'/>");
            out.println("<input type='submit' value='Добавить телефон'/>");
            out.println("</form>");
        } else if ("search".equals(action)) {
            out.println("<form method='post'>");
            out.println("Имя для поиска: <input type='text' name='searchName'/>");
            out.println("<input type='hidden' name='action' value='search'/>");
            out.println("<input type='submit' value='Искать'/>");
            out.println("</form>");
        } else {
            out.println("<a href='?action=addUser'>Добавить пользователя</a> | ");
            out.println("<a href='?action=addPhone'>Добавить телефон</a> | ");
            out.println("<a href='?action=search'>Поиск по имени</a><br/><br/>");

            for (var entry : notebook.getAll().entrySet()) {
                out.println("<b>" + entry.getKey() + "</b>: " + String.join(", ", entry.getValue()) + "<br/>");
            }
        }

        out.println("</body></html>");
    }

    @Override
    protected synchronized void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        String username = req.getParameter("username");

        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();

        if ("search".equals(action)) {
            String nameToSearch = req.getParameter("searchName");

            out.println("<html><head><title>Результат поиска</title></head><body>");
            out.println("<h2>Результат поиска: " + nameToSearch + "</h2>");

            var phones = notebook.getAll().get(nameToSearch);
            if (phones != null) {
                out.println("<b>" + nameToSearch + "</b>: " + String.join(", ", phones) + "<br/>");
            } else {
                out.println("Пользователь <b>" + nameToSearch + "</b> не найден.");
            }

            out.println("<br/><a href='" + req.getContextPath() + "/NotebookServlet'>Вернуться на главную</a>");
            out.println("</body></html>");
            return;
        }

        if ("addUser".equals(action)) {
            notebook.addUser(username);
        } else if ("addPhone".equals(action)) {
            String phone = req.getParameter("phone");
            notebook.addPhone(username, phone);
        }

        try {
            notebook.saveToFile(filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        resp.sendRedirect(req.getContextPath() + "/NotebookServlet");
    }
}
