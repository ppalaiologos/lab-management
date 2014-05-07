package servlet01;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.StringTokenizer;

import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

public class exam_servlet extends HttpServlet
{
	static final long serialVersionUID= 1L;
	private DataSource datasource = null;
	
	public void init() throws ServletException
	{
		try
		{
			InitialContext ic = new InitialContext();
			datasource = (DataSource)ic.lookup("java:comp/env/jdbc/mysqldb");
		}
		catch(Exception e)
		{
			throw new ServletException(e.toString());
		}
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		response.setContentType("text/html; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		request.setCharacterEncoding("UTF-8");
		String requestType = request.getParameter("requestType");
		PrintWriter out = response.getWriter();		
		try
		{
			Connection con = datasource.getConnection();
			Statement stmt = con.createStatement();
			if(requestType.startsWith("search"))
			{
				out.println("<html>");
				out.println("<head><title>Response ::"+requestType+"</title></head>");
				out.println("<body>");
				if(requestType.equalsIgnoreCase("searchPCProg"))
				{
					String prog_name= request.getParameter("specprogSear");
					ResultSet rs = stmt.executeQuery("SELECT pc_ID FROM installed WHERE prog_ID=\'"+prog_name+"\';");
					out.println("<table border=1>");
					out.println("<tr><th>PC number</th></tr>");
					while(rs.next())
					{
						out.println("<tr><td>"+rs.getInt("pc_ID")+"</td></tr>");
					}
					out.println("</table><a href=\"/Servlet/index.html\">Return</a></body></html>");
					rs.close();
					con.close();
					
				}
				else if(requestType.equalsIgnoreCase("searchPC"))
				{
					String seares = null;
					String pc2search = null;
					String pcs = request.getParameter("pcScope");
					if(pcs.equalsIgnoreCase("some"))
					{
						String pcsInts = request.getParameter("mult");
						StringTokenizer strtok = new StringTokenizer(pcsInts, ",");
						int cap = strtok.countTokens();
						for(int i = 0; i< cap ;i++)
						{
							if(pc2search==null)
							{
								pc2search = "";
								pc2search = " WHERE ID="+strtok.nextToken();
							}
							else
							{
								pc2search += " OR ID="+strtok.nextToken();
							}
						}
						pc2search +=";";
					}
					else
					{
						pc2search = ";";
					}//the where clause is done
					String reshtml = "<table border=1><tr><th>PC ID</th>";
					seares = "SELECT ID";
					boolean ch1=false,ch2=false,ch3=false,ch4=false,ch5=false;
					
					if(request.getParameter("pcRes_1")!=null)
					{
						seares+=", proc_type";
						reshtml+="<th>Processor type</th>";
						ch1=true;
					}
					if(request.getParameter("pcRes_2")!=null)
					{
						seares+=", RAM";
						reshtml+="<th>RAM capacity</th>";
						ch2=true;
					}
					if(request.getParameter("pcRes_3")!=null)
					{
						seares+=", hdspace";
						reshtml+="<th>HD space</th>";
						ch3=true;
					}
					if(request.getParameter("pcRes_4")!=null)
					{
						seares+=", MACaddr";
						reshtml+="<th>MAC address</th>";
						ch4=true;
					}
					if(request.getParameter("pcRes_5")!=null)
					{
						seares+=", name as Name";
						reshtml+="<th>Name</th>";
						ch5=true;
					}
					seares+=" ";
					reshtml+="</tr>";
					String query = seares + "FROM pc2" + pc2search;
					ResultSet rs = stmt.executeQuery(query);
					String rescol = reshtml;
					while(rs.next())
					{
						rescol +="<tr><td>"+rs.getInt("ID")+"</td>";
						if(ch1)
						{
							rescol += "<td>"+rs.getString("proc_type")+"</td>";
						}
						if(ch2)
						{
							rescol += "<td>"+rs.getInt("RAM")+"</td>";
						}
						if(ch3)
						{
							rescol +="<td>"+rs.getInt("hdspace")+"</td>";
						}
						if(ch4)
						{
							rescol +="<td>"+rs.getString("MACaddr")+"</td>";
						}
						if(ch5)
						{
							rescol +="<td>"+rs.getString("name")+"</td>";
						}
						rescol += "</tr>";
					}
					out.println(rescol);
					out.println("</table><a href=\"/Servlet/index.html\">Return</a></body></html>");
					rs.close();
					con.close();
				}
				else if(requestType.equalsIgnoreCase("searchLab"))
				{   
					boolean ch = false;
					String query = "SELECT DISTINCT progr_name";
					String reshtml = "<table border=1><tr><th>Program Name</th>";
					if(request.getParameter("labSearRes").equals("true"))
					{
						query += ", progr_type ";
						reshtml += "<th>Program Type</th></tr>";
						ch = true;
					}
					query += "FROM program A WHERE NOT EXISTS (SELECT ID FROM pc2 B WHERE NOT EXISTS (SELECT * FROM installed C WHERE A.progr_name = C.prog_ID AND B.ID = C.pc_ID" +"));";
					ResultSet rs = stmt.executeQuery(query);
					while(rs.next())
					{
						reshtml += "<tr><td>"+ rs.getString("prog_name")+"</td>";
						if(ch)
						{
							reshtml += "<td>"+rs.getString("prog_type")+"</td>";
						}
						reshtml += "</tr>";
					}
					reshtml += "</table><a href=\"/Servlet/index.html\">Return</a></body></html>";
					out.println(reshtml);
					rs.close();
					con.close();
				}
				else if(requestType.equalsIgnoreCase("searchProg"))
				{
					String prog_name = request.getParameter("progSearName");
					ResultSet rs = stmt.executeQuery("SELECT * FROM program A where A.progr_name=\'"+prog_name+"\';");
					out.println("<table border=1><tr><th>Program Name</th><th>Program Type</th></tr>");
					while(rs.next())
					{
						out.println("<tr><td>"+rs.getString("progr_name")+"</td><td>"+rs.getString("progr_type")+"</td></tr>");
					}
					out.println("</table><a href=\"/Servlet/index.html\">Return</a></body></html>");
				}
				else if(requestType.equalsIgnoreCase("searchProgPC"))
				{
					String pc_ID = request.getParameter("specpcSear");
					ResultSet rs = stmt.executeQuery("SELECT A.prog_ID, B.progr_type FROM installed A, program B WHERE A.prog_ID=B.progr_name AND pc_ID=\'"+pc_ID+"\';");
					out.println("<table border=1><tr><th>Program Name</th><th>Program Type</th></tr>");
					while(rs.next())
					{
						out.println("<tr><td>"+rs.getString("prog_ID")+"</td><td>"+rs.getString("progr_type")+"</td></tr>");
					}
					out.println("</table><a href=\"/Servlet/index.html\">Return</a></body></html>");
				}
				else if(requestType.equalsIgnoreCase("searchAllProg"))
				{
					ResultSet rs = stmt.executeQuery("SELECT * FROM program;");
					out.println("<table border=1><tr><th>Program Name</th><th>Program Type</th></tr>");
					while(rs.next())
					{
						out.println("<tr><td>"+rs.getString("progr_name")+"</td><td>"+rs.getString("progr_type")+"</td></tr>");
					}
					out.println("</table><a href=\"/Servlet/index.html\">Return</a></body></html>");
				}
				else
				{
					createDynPage(response, "Internal Error");
				}
			}
			else
			{
				createDynPage(response, "Internal error");
			}
			con.close();
			stmt.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			createDynPage(response, "Error: "+e.toString());
		}
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		response.setContentType("text/html; charset=UTF-8");
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		String requestType= request.getParameter("requestType");
		try
		{
			Connection con = datasource.getConnection();
			Statement stmt = con.createStatement();
			if(requestType.startsWith("Insert"))
			{
				if(requestType.equalsIgnoreCase("InsertPCProg"))
				{
					int pc_ID = Integer.parseInt(request.getParameter("PC_ID"));
					String prog_name = request.getParameter("progName");
					String query = "INSERT INTO installed VALUES ("+pc_ID+", \'"+prog_name+"\');";
					try
					{
						stmt.executeUpdate(query);
						createDynPage(response,"SQL database update was successful");
					}
					catch(SQLException sqle)
					{
						createDynPage(response, "SQL Error: "+sqle);
					}
				}
				else if(requestType.equalsIgnoreCase("InsertPC"))
				{
					String query = "INSERT INTO pc2(proc_type, RAM, hdspace, MACaddr, name) VALUES (\'"+request.getParameter("procType")+"\', "+request.getParameter("ramCap")+", "+request.getParameter("hddCap")+", \'"+request.getParameter("macAddr")+"\', \'"+request.getParameter("pcName")+"\');";
					try
					{
						stmt.executeUpdate(query);
						createDynPage(response,"SQL database update was successful");
					}
					catch(SQLException sqle)
					{
						createDynPage(response, "SQL Error: "+sqle);
					}
				}
				else if(requestType.equalsIgnoreCase("InsertProg"))
				{
					String query = "INSERT INTO program VALUES (\'"+request.getParameter("progName")+"\', \'"+request.getParameter("progType")+"\');";
					try
					{
						stmt.executeUpdate(query);
						createDynPage(response,"SQL database update was successful");
					}
					catch(SQLException sqle)
					{
						createDynPage(response, "SQL Error: "+sqle);
					}
				}
				else
				{
					createDynPage(response, "Internal Error");
				}
			}
			else
			{
				createDynPage(response, "Internal Error");
			}
			con.close();
			stmt.close();
		}
		catch(Exception e)
		{
			createDynPage(response, "Database Connection Problem: "+e.toString());
			e.printStackTrace();
		}		
	}
	
	private void createDynPage(HttpServletResponse response, String message) throws IOException
	{
		response.setContentType("text/html; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();
		out.println("<html>");
		out.println("<head><title>Servlet :: Message</title></head>");
		out.println("<body>");
		out.println("<p>"+message+"</p><br/>");
		out.println("<a href=\"/Servlet/index.html\">Return</a>");
		out.println("</body></html>");
	}
}
