/*
 * Copyright 2016 Black Pepper Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.blackpepper.halclient;

import java.io.IOException;

import org.springframework.hateoas.Resource;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class EmbeddedChildDeserializer extends StdDeserializer<Object> implements ContextualDeserializer {
	
	private static final long serialVersionUID = -8694505834979017488L;

	private RestOperations restOperations;

	private ClientProxyFactory proxyFactory;
	
	public EmbeddedChildDeserializer(RestOperations restOperations, ClientProxyFactory proxyFactory) {
		this(Object.class, restOperations, proxyFactory);
	}
	
	private EmbeddedChildDeserializer(Class<?> type, RestOperations restOperations, ClientProxyFactory proxyFactory) {
		super(type);
		
		this.restOperations = restOperations;
		this.proxyFactory = proxyFactory;
	}

	@Override
	public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		JavaType resourceType = ctxt.getTypeFactory().constructParametrizedType(Resource.class, Resource.class,
				handledType());
		
		Object resource = p.getCodec().readValue(p, resourceType);
		
		return proxyFactory.create((Resource) resource, (Class) handledType(), restOperations);
	}

	@Override
	public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property)
			throws JsonMappingException {
		return new EmbeddedChildDeserializer(ctxt.getContextualType().getRawClass(), restOperations, proxyFactory);
	}
	
	RestOperations getRestOperations() {
		return restOperations;
	}
	
	ClientProxyFactory getProxyFactory() {
		return proxyFactory;
	}
}
