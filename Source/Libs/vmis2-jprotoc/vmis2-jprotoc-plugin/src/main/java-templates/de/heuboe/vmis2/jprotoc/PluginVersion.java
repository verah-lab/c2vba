package de.heuboe.vmis2.jprotoc;

/**
 * Contains information about the plugin and its build time.
 */
public final class PluginVersion {

	private static final String	VERSION		= "${project.version}";
	private static final String	GROUPID		= "${project.groupId}";
	private static final String	ARTIFACTID	= "${project.artifactId}";
	private static final String	TIMESTAMP	= "${timestamp}";

	public String getVersion() {
		return VERSION;
	}

	public String getGroupId() {
		return GROUPID;
	}

	public String getArtifactId() {
		return ARTIFACTID;
	}

	public String getBuildTime() {
		if (null == TIMESTAMP) {
			return "Build time not available.";
		} else {
			return TIMESTAMP;
		}
	}

}
