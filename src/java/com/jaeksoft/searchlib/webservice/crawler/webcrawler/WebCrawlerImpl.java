/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2012 Emmanuel Keller / Jaeksoft
 * 
 * http://www.open-search-server.com
 * 
 * This file is part of OpenSearchServer.
 *
 * OpenSearchServer is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * OpenSearchServer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenSearchServer. 
 *  If not, see <http://www.gnu.org/licenses/>.
 **/
package com.jaeksoft.searchlib.webservice.crawler.webcrawler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.WebServiceException;

import org.apache.commons.io.IOUtils;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.crawler.web.database.CredentialManager;
import com.jaeksoft.searchlib.crawler.web.database.PatternItem;
import com.jaeksoft.searchlib.crawler.web.database.PatternManager;
import com.jaeksoft.searchlib.crawler.web.database.UrlManager;
import com.jaeksoft.searchlib.crawler.web.database.UrlManager.SearchTemplate;
import com.jaeksoft.searchlib.crawler.web.process.WebCrawlMaster;
import com.jaeksoft.searchlib.crawler.web.screenshot.ScreenshotManager;
import com.jaeksoft.searchlib.query.ParseException;
import com.jaeksoft.searchlib.request.SearchRequest;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.web.ScreenshotServlet;
import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.CommonServices;
import com.jaeksoft.searchlib.webservice.crawler.CrawlerUtils;

public class WebCrawlerImpl extends CommonServices implements SoapWebCrawler {

	private WebCrawlMaster getCrawlMaster(String use, String login, String key) {
		try {
			Client client = getLoggedClient(use, login, key,
					Role.WEB_CRAWLER_START_STOP);
			ClientFactory.INSTANCE.properties.checkApi();
			return client.getWebCrawlMaster();
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (InterruptedException e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException(e);
		}
	}

	@Override
	public CommonResult runOnce(String use, String login, String key) {
		return CrawlerUtils.runOnce(getCrawlMaster(use, login, key));
	}

	@Override
	public CommonResult runForever(String use, String login, String key) {
		return CrawlerUtils.runForever(getCrawlMaster(use, login, key));
	}

	@Override
	public CommonResult stop(String use, String login, String key) {
		return CrawlerUtils.stop(getCrawlMaster(use, login, key));
	}

	@Override
	public CommonResult status(String use, String login, String key) {
		return CrawlerUtils.status(getCrawlMaster(use, login, key));
	}

	private SearchRequest getRequest(UrlManager urlManager, String host)
			throws SearchLibException, ParseException {
		SearchRequest searchRequest = urlManager
				.getSearchRequest(SearchTemplate.urlExport);
		searchRequest.setQueryString("*:*");
		if (host != null && host.length() > 0)
			searchRequest.addFilter("host:\"" + host + '"', false);
		return searchRequest;
	}

	@Override
	public byte[] exportURLs(String use, String login, String key) {
		try {
			ClientFactory.INSTANCE.properties.checkApi();
			Client client = getLoggedClientAnyRole(use, login, key,
					Role.GROUP_WEB_CRAWLER);
			File file = client.getUrlManager().exportURLs(
					getRequest(client.getUrlManager(), null));
			return IOUtils.toByteArray(new FileInputStream(file));
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (FileNotFoundException e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException(e);
		} catch (ParseException e) {
			throw new WebServiceException(e);
		} catch (InterruptedException e) {
			throw new WebServiceException(e);
		}
	}

	@Override
	public byte[] exportSiteMap(String use, String host, String login,
			String key) {
		try {
			Client client = getLoggedClientAnyRole(use, login, key,
					Role.GROUP_WEB_CRAWLER);
			ClientFactory.INSTANCE.properties.checkApi();
			File file = client.getUrlManager().exportSiteMap(
					getRequest(client.getUrlManager(), host));
			return IOUtils.toByteArray(new FileInputStream(file));
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (FileNotFoundException e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException(e);
		} catch (ParseException e) {
			throw new WebServiceException(e);
		} catch (InterruptedException e) {
			throw new WebServiceException(e);
		}
	}

	@Override
	public CommonResult injectPatterns(String use, String login, String key,
			Boolean deleteAll, List<String> injectList) {
		try {
			long count = 0;
			Client client = getLoggedClientAnyRole(use, login, key,
					Role.GROUP_WEB_CRAWLER);
			ClientFactory.INSTANCE.properties.checkApi();
			List<PatternItem> patternList = new ArrayList<PatternItem>();
			for (String inject : injectList) {
				patternList = inject(client.getInclusionPatternManager(),
						inject, deleteAll);
				count++;
			}
			addPatternList(patternList, client.getUrlManager());
			return new CommonResult(true, count + "patterns injected");
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (InterruptedException e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException(e);
		}
	}

	private void addPatternList(List<PatternItem> patternList,
			UrlManager urlManager) throws SearchLibException {
		if (patternList == null)
			return;
		else
			urlManager.injectPrefix(patternList);
	}

	private List<PatternItem> inject(PatternManager patternManager,
			String patternTextList, boolean bDeleteAll)
			throws SearchLibException {
		List<PatternItem> patternList = PatternManager
				.getPatternList(patternTextList);
		patternManager.addList(patternList, bDeleteAll);
		return patternList;
	}

	@Override
	public CommonResult captureScreenshot(String use, String login, String key,
			URL url) {
		try {
			Client client = getLoggedClientAnyRole(use, login, key,
					Role.GROUP_WEB_CRAWLER);
			ClientFactory.INSTANCE.properties.checkApi();
			ScreenshotManager screenshotManager = client.getScreenshotManager();
			CredentialManager credentialManager = client
					.getWebCredentialManager();
			ScreenshotServlet.doCapture(null, screenshotManager,
					credentialManager, url);
			String message = "Captured URL " + url;
			return new CommonResult(true, message);
		} catch (MalformedURLException e) {
			throw new WebServiceException(e);
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (InterruptedException e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException(e);
		}
	}

	@Override
	public CommonResult checkScreenshot(String use, String login, String key,
			URL url) {
		try {
			Client client = getLoggedClientAnyRole(use, login, key,
					Role.GROUP_WEB_CRAWLER);
			ClientFactory.INSTANCE.properties.checkApi();
			ScreenshotManager screenshotManager = client.getScreenshotManager();
			String message = ScreenshotServlet.doCheck(screenshotManager, url);
			return new CommonResult(true, message);
		} catch (MalformedURLException e) {
			throw new WebServiceException(e);
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (InterruptedException e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException(e);
		}
	}

}
