import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.Objects;

import com.google.gson.Gson;
import model.ResponseMsg;
import model.SwipeDetail;

public class TwinderServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // Gather Path Info
            String pathInfo = request.getPathInfo();
            StringBuilder builder = new StringBuilder(pathInfo);
            builder.deleteCharAt(0);
            pathInfo = builder.toString();
            String[] parts = pathInfo.split("/");

            // Gather Body Info
            StringBuilder sb = new StringBuilder();
            String s;
            while ((s = request.getReader().readLine()) != null) {
                sb.append(s);
            }
            Gson gson = new Gson();
            SwipeDetail swipeDetail = (SwipeDetail) gson.fromJson(sb.toString(), SwipeDetail.class);

            // Use Path and Body for error check
            if (parts.length != 1){
                sendInvalidInputResponse(response);
            } else if ((!Objects.equals(parts[0], "left")) && (!Objects.equals(parts[0], "right"))) {
                sendInvalidInputResponse(response);
            } else if ((Integer.parseInt(swipeDetail.getSwiper()) > 5000) ||
                    (Integer.parseInt(swipeDetail.getSwiper()) < 1) ||
                    (Integer.parseInt(swipeDetail.getSwipee()) > 1000000) ||
                    (Integer.parseInt(swipeDetail.getSwipee()) < 1)) {
                sendUserNotFoundResponse(response);
            } else {
                sendSuccessResponse(response);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            sendInvalidInputResponse(response);
        }
    }

    private void sendInvalidInputResponse(HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        Gson gson = new Gson();
        ResponseMsg message = new ResponseMsg();
        String invalid_input_msg = "Invalid inputs";
        message.setMessage(invalid_input_msg);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getOutputStream().print(gson.toJson(message));
        response.getOutputStream().flush();
    }

    private void sendSuccessResponse(HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        Gson gson = new Gson();
        ResponseMsg message = new ResponseMsg();
        String success_msg = "Write successful";
        message.setMessage(success_msg);
        response.setStatus(HttpServletResponse.SC_CREATED);
        response.getOutputStream().print(gson.toJson(message));
        response.getOutputStream().flush();
    }

    private void sendUserNotFoundResponse(HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        Gson gson = new Gson();
        ResponseMsg message = new ResponseMsg();
        String user_not_found_msg = "User not found";
        message.setMessage(user_not_found_msg);
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.getOutputStream().print(gson.toJson(message));
        response.getOutputStream().flush();
    }
}
