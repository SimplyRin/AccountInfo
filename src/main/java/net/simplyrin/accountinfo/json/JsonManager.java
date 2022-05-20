package net.simplyrin.accountinfo.json;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import lombok.var;

/**
 * Created by SimplyRin on 2022/05/21.
 *
 * Copyright (c) 2022 SimplyRin
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
public class JsonManager {
	
	public static JsonObject getJson(File file) {
		try {
			var bytes = Files.readAllBytes(file.toPath());
			var value = new String(bytes);
			
			if (value != null && value.length() >= 5) {
				return new JsonParser().parse(value).getAsJsonObject();
			}
		} catch (IOException e) {
		}
		
		return new JsonObject();
	}
	
	public static void saveJson(JsonObject json, File file) {
		var builder = new GsonBuilder().setPrettyPrinting().create();
		var value = builder.toJson(json);
		
		if (value == null || (value != null && value.equals("null"))) {
			return;
		}
		
		try {
			var fw = new FileWriter(file);
			fw.write(value);
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
