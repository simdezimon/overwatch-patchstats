package com.simdezimon.overwatch;

import com.google.gson.annotations.SerializedName;

public class PlayerId {
	private String tag;
	private String region;
	private String platform;
	
	public PlayerId() {
	}
	
	public PlayerId(String tag, String region, String platform) {
		this.tag = tag;
		this.region = region;
		this.platform = platform;
	}
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((platform == null) ? 0 : platform.hashCode());
		result = prime * result + ((region == null) ? 0 : region.hashCode());
		result = prime * result + ((tag == null) ? 0 : tag.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof PlayerId))
			return false;
		PlayerId other = (PlayerId) obj;
		if (platform == null) {
			if (other.platform != null)
				return false;
		} else if (!platform.equals(other.platform))
			return false;
		if (region == null) {
			if (other.region != null)
				return false;
		} else if (!region.equals(other.region))
			return false;
		if (tag == null) {
			if (other.tag != null)
				return false;
		} else if (!tag.equals(other.tag))
			return false;
		return true;
	}

	public String getTag() {
		return tag;
	}

	public String getRegion() {
		return region;
	}

	public String getPlatform() {
		return platform;
	}

	@Override
	public String toString() {
		return tag + "-" + region + "-" + platform;
	}
	
	
}
