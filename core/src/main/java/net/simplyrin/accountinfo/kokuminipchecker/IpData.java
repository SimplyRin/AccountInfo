package net.simplyrin.accountinfo.kokuminipchecker;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import lombok.Getter;

/**
 * Created by SimplyRin on 2021/01/17.
 *
 * Copyright (c) 2021 SimplyRin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software") @Expose, to deal
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
@Getter
public class IpData {

	@SerializedName("status") @Expose
	private String status;
	@SerializedName("continent") @Expose
	private String continent;
	@SerializedName("continentCode") @Expose
	private String continentCode;
	@SerializedName("country") @Expose
	private String country;
	@SerializedName("countryCode") @Expose
	private String countryCode;
	@SerializedName("region") @Expose
	private String region;
	@SerializedName("regionName") @Expose
	private String regionName;
	@SerializedName("city") @Expose
	private String city;
	@SerializedName("district") @Expose
	private String district;
	@SerializedName("zip") @Expose
	private String zip;
	@SerializedName("lat") @Expose
	private Double lat;
	@SerializedName("lon") @Expose
	private Double lon;
	@SerializedName("timezone") @Expose
	private String timezone;
	@SerializedName("offset") @Expose
	private Integer offset;
	@SerializedName("currency") @Expose
	private String currency;
	@SerializedName("isp") @Expose
	private String isp;
	@SerializedName("org") @Expose
	private String org;
	@SerializedName("as") @Expose
	private String as;
	@SerializedName("asname") @Expose
	private String asName;
	@SerializedName("reverse") @Expose
	private String reverse;
	@SerializedName("mobile") @Expose
	private Boolean mobile;
	@SerializedName("proxy") @Expose
	private Boolean proxy;
	@SerializedName("hosting") @Expose
	private Boolean hosting;
	@SerializedName("query") @Expose
	private String query;

}
