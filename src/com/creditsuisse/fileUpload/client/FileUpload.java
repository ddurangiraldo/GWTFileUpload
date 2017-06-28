package com.creditsuisse.fileUpload.client;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class FileUpload extends HttpServlet {
	
	private static final Pattern titlePattern = Pattern.compile("(?s)(?i)\\.(pdf|doc|docx|xls|png|jpg|jpeg)|untitled");
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ServletFileUpload upload = new ServletFileUpload();

		try { 
			FileItemIterator iter = upload.getItemIterator(request);

			while (iter.hasNext()) {
				FileItemStream item = iter.next();

				String name = item.getName();
				InputStream stream = item.openStream();

				// Process the input stream
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				int len;
				byte[] buffer = new byte[8192];
				while ((len = stream.read(buffer, 0, buffer.length)) != -1) {
					out.write(buffer, 0, len);
				}

				int maxFileSize = 10 * (1024 * 1024); // 10 megs max
				if (out.size() > maxFileSize) {
					throw new RuntimeException("File is > than " + maxFileSize);
				}

				if (name != null && titlePattern.matcher(name).find()) {
					String[] path = name.split("\\");
					try (OutputStream outputStream = new FileOutputStream(path[path.length-1])) {
						out.writeTo(outputStream);
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}
}
