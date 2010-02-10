package org.sakaiproject.content.impl.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.test.SakaiKernelTestBase;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

public class ContentHostingServiceTest extends SakaiKernelTestBase {

	private static final String SIMPLE_FOLDER1 = "/admin/folder1/";
	private static final Log log = LogFactory.getLog(ContentHostingServiceTest.class);
	
	
	public static Test suite()
	{
		TestSetup setup = new TestSetup(new TestSuite(ContentHostingServiceTest.class))
		{
			protected void setUp() throws Exception 
			{
				log.debug("starting oneTimeSetup");
				oneTimeSetup(null);
				log.debug("finished oneTimeSetup");
			}
			protected void tearDown() throws Exception 
			{
				log.debug("starting tearDown");
				oneTimeTearDown();
				log.debug("finished tearDown");
			}
		};
		return setup;
	}
	
	
	/**
	 * Checks the resources of zero bytes are handled correctly.
	 */
	public void testEmptyResources() throws Exception {
		ContentHostingService ch = org.sakaiproject.content.cover.ContentHostingService.getInstance();
		SessionManager sm = org.sakaiproject.tool.cover.SessionManager.getInstance();
		Session session = sm.getCurrentSession();
		session.setUserEid("admin");
		session.setUserId("admin");
		ContentResourceEdit cr;
		cr = ch.addResource("/emptyFileStreamed");
		cr.setContent(new ByteArrayInputStream(new byte[0]));
		ch.commitResource(cr);
		
		cr = ch.addResource("/emptyFileArray");
		cr.setContent(new byte[0]);
		ch.commitResource(cr);
		
		ContentResource resource;
		InputStream stream;
		resource = ch.getResource("/emptyFileStreamed");
		stream = resource.streamContent();
		assertEquals(0, stream.available());
		assertEquals(0, resource.getContentLength());
		assertEquals(0, resource.getContent().length);
		
		resource = ch.getResource("/emptyFileArray");
		stream = resource.streamContent();
		assertEquals(0, stream.available());
		assertEquals(0, resource.getContentLength());
		assertEquals(0, resource.getContent().length);
		
		
	}
	
	
	public void testSaveRetriveFolder() {
		ContentHostingService ch = org.sakaiproject.content.cover.ContentHostingService.getInstance();
		
		try {
			ContentCollectionEdit ce = ch.addCollection(SIMPLE_FOLDER1);
			ch.commitCollection(ce);
			log.info("commited folder:" + ce.getId());
		} catch (IdUsedException e) {
			e.printStackTrace();
			fail("Got an id Used exception!");
		} catch (IdInvalidException e) {
			e.printStackTrace();
			fail("That id is invalid!");
		} catch (PermissionException e) {
			e.printStackTrace();
			fail();
		} catch (InconsistentException e) {
			e.printStackTrace();
			fail();
		}
		
		
		//now try retrieve the folder
		try {
			ContentCollection cc = ch.getCollection(SIMPLE_FOLDER1);
			assertNotNull(cc);
		} catch (IdUnusedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		} catch (TypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		} catch (PermissionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		}
		
		//lets test saving a utf8
		String utf8Folder = String.valueOf("\u6c92\u6709\u5df2\u9078\u8981\u522a\u9664\u7684\u9644\u4ef6");
		String utfId = "/admin/" + utf8Folder + "/";
		try {
			ContentCollectionEdit cce = ch.addCollection(utfId);
			ch.commitCollection(cce);
			log.info("commited folder:" + cce.getId());
		} catch (IdUsedException e) {
			e.printStackTrace();
		} catch (IdInvalidException e) {
			e.printStackTrace();
			fail();
		} catch (PermissionException e) {
			e.printStackTrace();
			fail();
		} catch (InconsistentException e) {
			e.printStackTrace();
			fail();
		}
		
		//now try retrieve the folder
		try {
			ContentCollection cc = ch.getCollection(utfId);
			assertNotNull(cc);
		} catch (IdUnusedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		} catch (TypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		} catch (PermissionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		}
		
	}
}