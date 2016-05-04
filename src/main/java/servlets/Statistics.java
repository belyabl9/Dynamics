package servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;

import com.google.gson.Gson;

import domain.Event;
import domain.Session;
import domain.UserStatistics;
import service.Features;
import service.Sessions;

/**
 * Servlet implementation class Statistics
 */

@WebServlet("/stat")
public class Statistics extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
	private static String STATISTICS_PER_USER_PAGE = "/statPerUser.jsp";
	
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Statistics() {
        super();
    }

    
    private List<UserStatistics> getStatisticsPerUser() {
		List<UserStatistics> statList = new ArrayList<UserStatistics>();
		Map<Long, List<Session>> sessionsPerUser = Sessions.getSessionsPerUser();
		for (Long userId : sessionsPerUser.keySet()) {
			List<Session> sessions = sessionsPerUser.get(userId);
			UserStatistics statistics = new UserStatistics();
			statistics.setUser(service.Users.findById(userId));
			for (Session session : sessions) {
				List<Event> events = session.getEvents();
				statistics.addKeypressTime(Features.getMeanKeyTime(events));
				statistics.addTimeBetweenKeypress(Features.getMeanTimeBetweenKeys(events));
			}
			statList.add(statistics);
		}
		
		return statList;
    }
    
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		List<UserStatistics> statList = getStatisticsPerUser();
		String statJSON = new Gson().toJson(statList);
		request.setAttribute("statisticsJSON", StringEscapeUtils.unescapeEcmaScript(statJSON));
		request.getRequestDispatcher(STATISTICS_PER_USER_PAGE).forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
