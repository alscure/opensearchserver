/**   
 * License Agreement for OpenSearchServer
 *
 * Copyright (C) 2011-2013 Emmanuel Keller / Jaeksoft
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
package com.jaeksoft.searchlib.webservice.schema;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;
import javax.xml.ws.WebServiceException;

import com.jaeksoft.searchlib.Client;
import com.jaeksoft.searchlib.ClientCatalog;
import com.jaeksoft.searchlib.ClientCatalogItem;
import com.jaeksoft.searchlib.ClientFactory;
import com.jaeksoft.searchlib.SearchLibException;
import com.jaeksoft.searchlib.schema.SchemaField;
import com.jaeksoft.searchlib.schema.SchemaFieldList;
import com.jaeksoft.searchlib.template.TemplateAbstract;
import com.jaeksoft.searchlib.template.TemplateList;
import com.jaeksoft.searchlib.user.Role;
import com.jaeksoft.searchlib.user.User;
import com.jaeksoft.searchlib.web.SchemaServlet;
import com.jaeksoft.searchlib.webservice.CommonResult;
import com.jaeksoft.searchlib.webservice.CommonServices;

public class SchemaImpl extends CommonServices implements Schema {

	@Override
	public CommonResult deleteIndex(String login, String key, String indexName) {
		try {
			User user = getLoggedUser(login, key);
			ClientFactory.INSTANCE.properties.checkApi();
			ClientCatalog.eraseIndex(user, indexName);
			return new CommonResult(true, "Index deleted: " + indexName);
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException(e);
		} catch (NamingException e) {
			throw new WebServiceException(e);
		} catch (InterruptedException e) {
			throw new WebServiceException(e);
		}
	}

	@Override
	public CommonResult createIndex(String login, String key, String indexName,
			TemplateList indexTemplateName) {
		try {
			User user = getLoggedUser(login, key);
			ClientFactory.INSTANCE.properties.checkApi();
			if (user != null && !user.isAdmin())
				throw new WebServiceException("Not allowed");
			TemplateAbstract template = TemplateList
					.findTemplate(indexTemplateName.name());
			ClientCatalog.createIndex(user, indexName, template);
			return new CommonResult(true, "Created Index " + indexName);
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException(e);
		} catch (InterruptedException e) {
			throw new WebServiceException(e);
		}
	}

	@Override
	public List<String> indexList(String login, String key) {
		try {
			User user = getLoggedUser(login, key);
			ClientFactory.INSTANCE.properties.checkApi();
			List<String> indexList = new ArrayList<String>();
			for (ClientCatalogItem catalogItem : ClientCatalog
					.getClientCatalog(user))
				indexList.add(catalogItem.getIndexName());
			return indexList;
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (InterruptedException e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException(e);
		}
	}

	@Override
	public CommonResult setField(String use, String login, String key,
			SchemaFieldRecord schemaFieldRecord) {
		try {
			Client client = getLoggedClient(use, login, key, Role.INDEX_SCHEMA);
			ClientFactory.INSTANCE.properties.checkApi();
			setField(client, schemaFieldRecord);
			return new CommonResult(true, "Added Field "
					+ schemaFieldRecord.name);
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (InterruptedException e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException(e);
		}
	}

	private void setField(Client client, SchemaFieldRecord schemaFieldRecord)
			throws SearchLibException {
		com.jaeksoft.searchlib.schema.Schema schema = client.getSchema();
		SchemaField schemaField = new SchemaField();
		schemaField.setIndexAnalyzer(schemaFieldRecord.indexAnalyzer);
		schemaField.setIndexed(schemaFieldRecord.indexed);
		schemaField.setName(schemaFieldRecord.name);
		schemaField.setStored(schemaFieldRecord.stored);
		schemaField.setTermVector(schemaFieldRecord.termVector);
		schema.getFieldList().put(schemaField);
		SchemaServlet.saveSchema(client, schema);
	}

	@Override
	public CommonResult deletefield(String use, String login, String key,
			String deleteField) {
		try {
			Client client = getLoggedClient(use, login, key, Role.INDEX_SCHEMA);
			ClientFactory.INSTANCE.properties.checkApi();
			String message = delete(client, use, deleteField);
			return new CommonResult(true, message);
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (InterruptedException e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException(e);
		}
	}

	private String delete(Client client, String use, String deleteField)
			throws SearchLibException {
		com.jaeksoft.searchlib.schema.Schema schema = client.getSchema();
		SchemaFieldList sfl = schema.getFieldList();
		SchemaField field = sfl.get(deleteField.trim());
		if (field == null)
			return "Nothing to delete";
		sfl.remove(field.getName());
		SchemaServlet.saveSchema(client, schema);
		return "Deleted " + deleteField;
	}

	@Override
	public CommonResult setDefaultField(String use, String login, String key,
			String defaultField) {
		try {
			Client client = getLoggedClient(use, login, key, Role.INDEX_SCHEMA);
			ClientFactory.INSTANCE.properties.checkApi();
			com.jaeksoft.searchlib.schema.Schema schema = client.getSchema();
			schema.getFieldList().setDefaultField(defaultField);
			SchemaServlet.saveSchema(client, schema);
			String message = "Default field has been set to " + defaultField;
			return new CommonResult(true, message);
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (InterruptedException e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException(e);
		}
	}

	@Override
	public CommonResult setUniqueField(String use, String login, String key,
			String uniqueField) {
		try {
			Client client = getLoggedClient(use, login, key, Role.INDEX_SCHEMA);
			ClientFactory.INSTANCE.properties.checkApi();
			com.jaeksoft.searchlib.schema.Schema schema = client.getSchema();
			schema.getFieldList().setUniqueField(uniqueField);
			SchemaServlet.saveSchema(client, schema);
			String message = "Unique field has been set to " + uniqueField;
			return new CommonResult(true, message);
		} catch (SearchLibException e) {
			throw new WebServiceException(e);
		} catch (InterruptedException e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException(e);
		}
	}

	@Override
	public List<SchemaFieldRecord> getFieldList(String use, String login,
			String key) {
		try {
			Client client = getLoggedClient(use, login, key, Role.INDEX_SCHEMA);
			ClientFactory.INSTANCE.properties.checkApi();
			com.jaeksoft.searchlib.schema.Schema schema = client.getSchema();
			List<SchemaFieldRecord> fieldList = new ArrayList<SchemaFieldRecord>();
			for (SchemaField schemaField : schema.getFieldList().getList()) {
				SchemaFieldRecord fieldListResult = new SchemaFieldRecord(
						schemaField.getName(), schemaField.getIndexAnalyzer(),
						schemaField.getIndexed(), schemaField.getStored(),
						schemaField.getTermVector());
				fieldList.add(fieldListResult);
			}
			return fieldList;
		} catch (InterruptedException e) {
			throw new WebServiceException(e);
		} catch (IOException e) {
			throw new WebServiceException(e);
		}
	}

}
