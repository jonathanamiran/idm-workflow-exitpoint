package idm.wf.tools;


import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;


import com.infor.daf.icp.CMAttribute;
import com.infor.daf.icp.CMItem;
import com.infor.daf.icp.CMResource;
import com.infor.daf.icp.Connection;
import com.infor.daf.icp.Connection.AuthenticationMode;

import java.sql.ResultSet;
import java.sql.Statement;

public class GetMingleAttachments {

	

	public static void main(String[] args) {

		GetMingleAttachments doit = new GetMingleAttachments();

		Map<String, String> attr = new HashMap<>();

		String CUNO = "TMP001";
		String WorkflowId = "97";
		String DocType = "MDS_GenericDocument";
		attr.put("MDS_id2", CUNO);
		attr.put("MDS_id3", WorkflowId);
		attr.put("MDS_EntityType", "WorkflowAttachment");

		// System.out.println(attr.);

		doit.lstWFattachments(WorkflowId, DocType, attr);

	}

	public void lstWFattachments(String WorkFlowID, String DocType, Map<String, String> attr) {

		try {
			java.sql.DriverManager.registerDriver(new com.microsoft.sqlserver.jdbc.SQLServerDriver());

			String dbServer = "db\\\\inforos";
			String dbName = "Infor_IOS_Mingle";
			String dbUser = "xiuser";
			String dbPassword = "*******";
			String url = "jdbc:sqlserver://" + dbServer + ";integratedSecurity=false;databasename=" + dbName + ";user="
					+ dbUser + ";password=" + dbPassword;

			java.sql.Connection conn = java.sql.DriverManager.getConnection(url);

			Statement stmt = conn.createStatement();
			ResultSet rs;

			String strSQL = "";

			strSQL = " SELECT [AttachmentId]  ";
			strSQL += " ,[AttachmentType]  ";
			strSQL += " ,[RelKey] ";
			strSQL += " ,[FolderPath] ";
			strSQL += " ,[FileSystemName] ";
			strSQL += " ,[ActualFileName] ";
			strSQL += " ,[IsImage] ";
			strSQL += " ,[Size] ";
			strSQL += " ,[UserId] ";
			strSQL += " ,[StatusId] ";
			strSQL += " ,[UpdatedOn] ";
			strSQL += " ,[CreatedOn] ";
			strSQL += " FROM [" + dbName + "].[dbo].[MingleAttachment]  ";
			strSQL += " where RelKey like 'TASKCONTEXT" + WorkFlowID + "' ";

			rs = stmt.executeQuery(strSQL);
			System.out.println(strSQL);
			while (rs.next()) {
				CMItem item = new CMItem();				
				item.setEntityName(DocType);

				System.out.println("FolderPath: " + rs.getString("FolderPath").trim());
				String filePath = rs.getString("FolderPath").trim();
				String fileName = rs.getString("FileSystemName").trim();
				for (String key : attr.keySet()) {
					item.getAttributes().add(new CMAttribute(key, attr.get(key)));

				}
				AddAttachmentToIDM(item, filePath, fileName);
			}

			rs.close();
			conn.close();
		} catch (Exception e) {
			// System.err.println("Got an exception! ");
			e.printStackTrace();
			System.out.println(e.getMessage());
		}

	}

	public GetMingleAttachments() {
		// scanFolder("D:\\Infor\\SupplierInvoiceScanner\\in");

	}

	public boolean AddAttachmentToIDM(CMItem item, String filePath, String fileName) {
		System.out.println("AddAttachmentToIDM");
		try {
			// Create and connect the connection
			Connection conn = new Connection((String) null, "D:\\Infor\\WorkflowIDM\\JavaIDMUpload.ionapi",
					"JavaIDMUpload.ionapi", AuthenticationMode.OAUTH2);
			// Connection conn3 = new Connection((String)null, "<.ionapi filepath>",
			// "<.ionapi Json file>", AuthenticationMode.OAUTH2);
			System.out.println("AddAttachmentToIDM " + conn);
			conn.connect();
			System.out.println("AddAttachmentToIDM connect ");
			
			// Add a file, if a stream is already available then this method can be used to
			
			System.out.println("AddAttachmentToIDM byteArr ");
			byte[] byteArr = Files.readAllBytes(Paths.get(filePath + "\\" + fileName));
			CMResource res = new CMResource(fileName, byteArr);
			
			// CMResource res = new CMResource("fish.jpeg", byteArr);
			item.getResources().add(res);
			System.out.println("AddAttachmentToIDM getResources ");

			// Add the item and print the item pid that the item has been updated with
			item.add(conn);
			System.out.println("Item was added successfully.");
			System.out.println("Pid: " + item.getPid());
			System.out.println("Main resource url: " + item.getResources().get(CMResource.MAIN).getUrl());


			conn.disconnect();
			return true;
		} catch (Exception e) {
			System.out.println("AddAttachmentToIDM CATCH");
			e.printStackTrace(System.out);

			return false;
		}
	}

	

}
