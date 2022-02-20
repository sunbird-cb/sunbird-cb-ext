/*
 *                "Copyright 2020 Infosys Ltd.
 *                Use of this source code is governed by GPL v3 license that can be found in the LICENSE file or at https://opensource.org/licenses/GPL-3.0
 *                This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License version 3"
 *
 */

package org.sunbird.common.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Response implements Serializable {

	private static final long serialVersionUID = -3773253896160786443L;

	private transient Map<String, Object> result = new HashMap<>();

	public boolean containsKey(String key) {
		return result.containsKey(key);
	}

	public Object get(String key) {
		return result.get(key);
	}

	public Map<String, Object> getResult() {
		return result;
	}

	public void put(String key, Object vo) {
		result.put(key, vo);
	}

	public void putAll(Map<String, Object> map) {
		result.putAll(map);
	}

}
