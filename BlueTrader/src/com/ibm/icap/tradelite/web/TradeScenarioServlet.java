package com.ibm.icap.tradelite.web;

import java.io.IOException;
import javax.servlet.ServletException;
//import javax.servlet.annotation.WebInitParam;
//import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.servlet.*;
import javax.servlet.http.*;
import java.util.Collection;
import java.util.Iterator;
import java.io.PrintWriter;

import com.ibm.icap.tradelite.*;
import com.ibm.icap.tradelite.util.*;

/**
 * Servlet implementation class TradeScenarioServlet
 */
//@WebServlet(
//		urlPatterns = "/scenario", 
//		initParams = { 
//				@WebInitParam(name = "runTimeMode", value = "EJB", description = "Sets the default RuntimeMode. Legal values include EJB and Direct"), 
//				@WebInitParam(name = "orderProcessingMode", value = "Synchronous"), 
//				@WebInitParam(name = "accessMode", value = "Standard"), 
//				@WebInitParam(name = "workloadMix", value = "Standard"), 
//				@WebInitParam(name = "WebInterface", value = "JSP"), 
//				@WebInitParam(name = "maxUsers", value = "500"), 
//				@WebInitParam(name = "maxQuotes", value = "1000"), 
//				@WebInitParam(name = "primIterations", value = "1"), 
//				@WebInitParam(name = "CachingType", value = "2"), 
//				@WebInitParam(name = "LongRun", value = "true")
//		})
public class TradeScenarioServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       


	   /**
		* Servlet initialization method.
		*/
		public void init(ServletConfig config) throws ServletException
		{
			super.init(config);
			java.util.Enumeration en = config.getInitParameterNames();
			while ( en.hasMoreElements() )
			{
				String parm = (String) en.nextElement();
				String value = config.getInitParameter(parm);
				TradeConfig.setConfigParam(parm, value);
			}
		}
		
	   /**
		* Returns a string that contains information about TradeScenarioServlet
		*
		* @return The servlet information
		*/
		public java.lang.String getServletInfo()
		{
			return "TradeScenarioServlet emulates a population of web users";
		}	



	   /**
		* Process incoming HTTP GET requests
		*
		* @param request Object that encapsulates the request to the servlet
		* @param response Object that encapsulates the response from the servlet
		*/
		public void doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response)
			throws ServletException, IOException
		{
			performTask(request,response);
		}

	   /**
		* Process incoming HTTP POST requests
		*
		* @param request Object that encapsulates the request to the servlet
		* @param response Object that encapsulates the response from the servlet
		*/
		public void doPost(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response)
			throws ServletException, IOException
		{
			performTask(request,response);
		}	

	   /** 
		* Main service method for TradeScenarioServlet
		*
		* @param request Object that encapsulates the request to the servlet
		* @param response Object that encapsulates the response from the servlet
		*/    
		public void performTask(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

			// Scenario generator for Trade2
			char action = ' ';
			String userID = null;

			// String to create full dispatch path to TradeAppServlet w/ request Parameters
			String dispPath = null; // Dispatch Path to TradeAppServlet

			String scenarioAction = (String) req.getParameter("action");
			if ((scenarioAction != null) && (scenarioAction.length() >= 1))
			{
				action = scenarioAction.charAt(0);
				if (action == 'n')
				{ //null;
					try
					{
						resp.setContentType("text/html");
						PrintWriter out = new PrintWriter(resp.getOutputStream());
						out.println("<HTML><HEAD>TradeScenarioServlet</HEAD><BODY>Hello</BODY></HTML>"); 
						out.close();
						return;
		
					}
					catch (Exception e)
					{
						Log.error(
							"trade_client.TradeScenarioServlet.service(...)" + 
							"error creating printwriter from responce.getOutputStream", e);
							
						resp.sendError(
								500, 
							"trade_client.TradeScenarioServlet.service(...): erorr creating and writing to PrintStream created from response.getOutputStream()"); 
					} //end of catch
		
				} //end of action=='n'
			}


			ServletContext ctx = null;
			HttpSession session = null;
			try
			{
				ctx = getServletConfig().getServletContext();
				// These operations require the user to be logged in. Verify the user and if not logged in
				// change the operation to a login
				session = req.getSession(true);
				userID = (String) session.getAttribute("uidBean");
			}
			catch (Exception e)
			{
				Log.error(
					"trade_client.TradeScenarioServlet.service(...): performing " + scenarioAction +
					"error getting ServletContext,HttpSession, or UserID from session" +
					"will make scenarioAction a login and try to recover from there", e);
				userID = null;
				action = 'l';
			}

			if (userID == null)
			{
				action = 'l'; // change to login
				TradeConfig.incrementScenarioCount();
			}
			else if (action == ' ') {
				//action is not specified perform a random operation according to current mix
				// Tell getScenarioAction if we are an original user or a registered user 
				// -- sellDeficits should only be compensated for with original users.
				action = TradeConfig.getScenarioAction(
					userID.startsWith(TradeConfig.newUserPrefix));
			}	
			switch (action)
				{

					case 'q' : //quote 
						dispPath = tasPathPrefix + "quotes&symbols=" + TradeConfig.rndSymbols();
						ctx.getRequestDispatcher(dispPath).include(req, resp);
						break;
					case 'a' : //account
						dispPath = tasPathPrefix + "account";
						ctx.getRequestDispatcher(dispPath).include(req, resp);
						break;
					case 'u' : //update account profile
						dispPath = tasPathPrefix + "account";
						ctx.getRequestDispatcher(dispPath).include(req, resp);

						String fullName = "rnd" + System.currentTimeMillis();
						String address = "rndAddress";
						String   password = "xxx";
						String email = "rndEmail";
						String creditcard = "rndCC";
						dispPath = tasPathPrefix + "update_profile&fullname=" + fullName + 
							"&password=" + password + "&cpassword=" + password + 					
							"&address=" + address +	"&email=" + email + 
							"&creditcard=" +  creditcard;
						ctx.getRequestDispatcher(dispPath).include(req, resp);
						break;
					case 'h' : //home
						dispPath = tasPathPrefix + "home";
						ctx.getRequestDispatcher(dispPath).include(req, resp);
						break;
					case 'l' : //login
						userID = TradeConfig.getUserID();
						String password2 = "xxx";
						dispPath = tasPathPrefix + "login&inScenario=true&uid=" + userID + "&passwd=" + password2;
						ctx.getRequestDispatcher(dispPath).include(req, resp);
							
						// login is successful if the userID is written to the HTTP session
						if (session.getAttribute("uidBean") == null) {
							System.out.println("TradeScenario login failed. Reset DB between runs");
						} 
						break;
					case 'o' : //logout
						dispPath = tasPathPrefix + "logout";
						ctx.getRequestDispatcher(dispPath).include(req, resp);
						break;
					case 'p' : //portfolio
						dispPath = tasPathPrefix + "portfolio";
						ctx.getRequestDispatcher(dispPath).include(req, resp);
						break;
					case 'r' : //register
						//Logout the current user to become a new user
						// see note in TradeServletAction
						req.setAttribute("TSS-RecreateSessionInLogout", Boolean.TRUE);
						dispPath = tasPathPrefix + "logout";
						ctx.getRequestDispatcher(dispPath).include(req, resp);

						userID = TradeConfig.rndNewUserID();
						String passwd = "yyy";
						fullName = TradeConfig.rndFullName();
						creditcard = TradeConfig.rndCreditCard();
						String money = TradeConfig.rndBalance();
						email = TradeConfig.rndEmail(userID);
						String smail = TradeConfig.rndAddress();
						dispPath = tasPathPrefix + "register&Full Name=" + fullName + "&snail mail=" + smail +
							"&email=" + email + "&user id=" + userID + "&passwd=" + passwd + 
							"&confirm passwd=" + passwd + "&money=" + money + 
							"&Credit Card Number=" + creditcard;
						ctx.getRequestDispatcher(dispPath).include(req, resp);
						break;
					case 's' : //sell
						dispPath = tasPathPrefix + "portfolioNoEdge";
						ctx.getRequestDispatcher(dispPath).include(req, resp);

						Collection holdings = (Collection) req.getAttribute("holdingDataBeans");
						int numHoldings = holdings.size();
						if (numHoldings > 0)
						{
							//sell first available security out of holding 
							
							Iterator it = holdings.iterator();
							boolean foundHoldingToSell = false;
							while (it.hasNext()) 
							{
								HoldingDataBean holdingData = (HoldingDataBean) it.next();
								if ( !(holdingData.getPurchaseDate().equals(new java.util.Date(0)))  )
								{
									Integer holdingID = holdingData.getHoldingID();

									dispPath = tasPathPrefix + "sell&holdingID="+holdingID;
									ctx.getRequestDispatcher(dispPath).include(req, resp);
									foundHoldingToSell = true;
									break;	
								}
							}
							if (foundHoldingToSell) break;
							if (Log.doTrace())
								Log.trace("TradeScenario: No holding to sell -switch to buy -- userID = " + userID + "  Collection count = " + numHoldings);		

						}
						// At this point: A TradeScenario Sell was requested with No Stocks in Portfolio
						// This can happen when a new registered user happens to request a sell before a buy
						// In this case, fall through and perform a buy instead

						/* Trade 2.037: Added sell_deficit counter to maintain correct buy/sell mix.
						 * When a users portfolio is reduced to 0 holdings, a buy is requested instead of a sell.
						 * This throws off the buy/sell mix by 1. This results in unwanted holding table growth
						 * To fix this we increment a sell deficit counter to maintain the correct ratio in getScenarioAction
						 * The 'z' action from getScenario denotes that this is a sell action that was switched from a buy
						 * to reduce a sellDeficit
						 */
						if (userID.startsWith(TradeConfig.newUserPrefix) == false)
						{
							TradeConfig.incrementSellDeficit();
						}
					case 'b' : //buy
						String symbol = TradeConfig.rndSymbol();
						String amount = TradeConfig.rndQuantity() + "";

						dispPath = tasPathPrefix + "quotes&symbols=" + symbol;
						ctx.getRequestDispatcher(dispPath).include(req, resp);

						dispPath = tasPathPrefix + "buy&quantity=" + amount + "&symbol=" + symbol;
						ctx.getRequestDispatcher(dispPath).include(req, resp);
						break;
				} //end of switch statement 
		}

		// URL Path Prefix for dispatching to TradeAppServlet
		private final static String tasPathPrefix = "/app?action=";



}
