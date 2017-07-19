package com.nokia.cloudband.cbnd.nsdgenerator.util;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StrSubstitutor;

public class TemplateUtil {
	
	private static final String NEED_VALUE = "NEED_VALUE";

	public TemplateUtil() {

	}

	private static String _loadFromFile(Map<String, String> model, String file) throws IOException {
		URL url = TemplateUtil.class.getResource(file);
		try {
			return StrSubstitutor.replace(FileUtils.readFileToString(new File(url.toURI()), Charset.defaultCharset()), model);
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}
	}

	public static String generateMetadata(String name, String version, String description, String yaml) throws IOException {
		Map<String, String> model = new HashMap<String, String>();
		model.put("meta_name", StringUtils.isEmpty(name) ? "" : name);
		model.put("meta_version", StringUtils.isEmpty(version) ? "" : "1.0");
		model.put("meta_description", StringUtils.isEmpty(description) ? "" : description);
		model.put("meta_main_yaml", yaml);
		return _loadFromFile(model, "/metadata.txt");
	}

	public static String generateYaml(String input, String node_template, String implementation_model, String placement) throws IOException {
		Map<String, String> model = new HashMap<String, String>();
		model.put("input", input);
		model.put("node_template", node_template);
		model.put("implementation_model", implementation_model);
		model.put("placement", placement);
		return _loadFromFile(model, "/main_yaml.txt");
	}
	
	public static String generateInputVnfHuawei(String name, String id, String vnfd_id_in_blueprint, String plan_name, String vendor, String vnfd_version) throws IOException {
		Map<String, String> model = new HashMap<String, String>();
		model.put("name", name);
		model.put("id", StringUtils.isEmpty(id) ? NEED_VALUE : id);
		model.put("vnfd_id_in_blueprint", StringUtils.isEmpty(vnfd_id_in_blueprint) ? NEED_VALUE : vnfd_id_in_blueprint);
		model.put("plan_name", StringUtils.isEmpty(plan_name) ? NEED_VALUE : plan_name);
		model.put("vendor", StringUtils.isEmpty(vendor) ? "HUAWEI" : vendor);
		model.put("vnfd_version", StringUtils.isEmpty(vnfd_version) ? NEED_VALUE : vnfd_version);
		return _loadFromFile(model, "/input_vnf_huawei.txt");
	}
	
	public static String generateInputExternalNeutron(String name, String cidr, String gateway, String dhcp, String vlan, String phys, String type) throws IOException {
		Map<String, String> model = new HashMap<String, String>();
		model.put("name", name);
		model.put("cidr", StringUtils.isEmpty(cidr) ? NEED_VALUE : cidr);
		model.put("gateway", StringUtils.isEmpty(gateway) ? NEED_VALUE : gateway);
		model.put("dhcp", BooleanUtils.toBoolean(dhcp) ? "true" : "false");
		if ("vlan".equalsIgnoreCase(type)) {
			model.put("vlan", StringUtils.isEmpty(vlan) ? NEED_VALUE : vlan);
		}
		model.put("phys", StringUtils.isEmpty(phys) ? NEED_VALUE : phys);
		if ("vlan".equalsIgnoreCase(type)) {
			return _loadFromFile(model, "/input_external_vlan_neutron.txt");
		} else {
			return _loadFromFile(model, "/input_external_flat_neutron.txt");
		}
		
	}
	
	public static String generateInputPredefineNeutron(String name, String uuid) throws IOException {
		Map<String, String> model = new HashMap<String, String>();
		model.put("name", name);
		model.put("uuid", StringUtils.isEmpty(uuid) ? NEED_VALUE : uuid);
		return _loadFromFile(model, "/input_predefine_neutron.txt");
	}
	
	public static String generateInputInternalNeutron(String name, String cidr, String dhcp) throws IOException {
		Map<String, String> model = new HashMap<String, String>();
		model.put("name", name);
		model.put("cidr", StringUtils.isEmpty(cidr) ? NEED_VALUE : cidr);
		model.put("dhcp", BooleanUtils.toBoolean(dhcp) ? "true" : "false");
		return _loadFromFile(model, "/input_internal_neutron.txt");
	}
	
	public static String generateVnfHuawei(String vnfName, String connection_point, String requirement, String virtual_link) throws IOException {
		Map<String, String> model = new HashMap<String, String>();
		model.put("name", vnfName);
		model.put("connection_point", connection_point);
		model.put("requirement", requirement);
		model.put("virtual_link", virtual_link);
		return _loadFromFile(model, "/node_template_vnf_huawei.txt");
	}
	
	public static String generateVnfHuaweiConnectinPoint(String cp_name, String nw_name, String nw_type) throws IOException {
		Map<String, String> model = new HashMap<String, String>();
		model.put("cp_name", cp_name);
		model.put("nw_name", nw_name);
		if ("predefine".equalsIgnoreCase(nw_type)) {
			return _loadFromFile(model, "/node_template_vnf_huawei_connection_point_predefine.txt");
		}
		return _loadFromFile(model, "/node_template_vnf_huawei_connection_point.txt");
	}
	
	public static String generateVnfRequirementVL(String cp_name, String nw_name) throws IOException {
		Map<String, String> model = new HashMap<String, String>();
		model.put("cp_name", cp_name);
		model.put("nw_name", nw_name);
		return _loadFromFile(model, "/node_template_vnf_requirement_vl.txt");
	}
	
	public static String generateVnfRequirementSubnet(String nw_name) throws IOException {
		Map<String, String> model = new HashMap<String, String>();
		model.put("nw_name", nw_name);
		return _loadFromFile(model, "/node_template_vnf_requirement_subnet.txt");
	}
	
	public static String generateVnfVL(String nw_name) throws IOException {
		Map<String, String> model = new HashMap<String, String>();
		model.put("nw_name", nw_name);
		return _loadFromFile(model, "/node_template_vnf_vl.txt");
	}
	
	public static String generateImplModelNuetronExternalNetwork(String nw_name, String type) throws IOException {
		Map<String, String> model = new HashMap<String, String>();
		model.put("nw_name", nw_name);
		if ("vlan".equalsIgnoreCase(type)) {
			return _loadFromFile(model, "/implementation_model_network_external_vlan_nuetron.txt");
		} else {
			return _loadFromFile(model, "/implementation_model_network_external_flat_nuetron.txt");
		}
	} 
	
	public static String generateImplModelNuetronInternalNetwork(String nw_name) throws IOException {
		Map<String, String> model = new HashMap<String, String>();
		model.put("nw_name", nw_name);
		return _loadFromFile(model, "/implementation_model_network_internal_nuetron.txt");
	} 

}
