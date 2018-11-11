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
		model.put("meta_version", StringUtils.isEmpty(version) ? "1.0" : version);
		model.put("meta_description", StringUtils.isEmpty(description) ? "" : description);
		model.put("meta_main_yaml", yaml);
		return _loadFromFile(model, "/metadata.txt");
	}

	public static String generateYaml(String input, String node_template, String virtual_link, String implementation_model, String placement) throws IOException {
		Map<String, String> model = new HashMap<String, String>();
		model.put("input", input);
		model.put("node_template", node_template);
		model.put("implementation_model", implementation_model);
		model.put("placement", placement);
		model.put("virtual_link", virtual_link);
		return _loadFromFile(model, "/main_yaml.txt");
	}

	public static String generateInputVnfHuawei(String name, String id, String vnfd_id_in_blueprint, String plan_name, String vendor, String vnfd_version, String input_variables) throws IOException {
		Map<String, String> model = new HashMap<String, String>();
		model.put("name", name);
		model.put("id", StringUtils.isEmpty(id) ? NEED_VALUE : id);
		model.put("vnfd_id_in_blueprint", StringUtils.isEmpty(vnfd_id_in_blueprint) ? NEED_VALUE : vnfd_id_in_blueprint);
		model.put("plan_name", StringUtils.isEmpty(plan_name) ? NEED_VALUE : plan_name);
		model.put("vendor", StringUtils.isEmpty(vendor) ? "HUAWEI" : vendor);
		model.put("vnfd_version", StringUtils.isEmpty(vnfd_version) ? NEED_VALUE : vnfd_version);
		model.put("input_variables", StringUtils.isEmpty(input_variables) ? "[]" : input_variables.trim());
		return _loadFromFile(model, "/input_vnf_huawei.txt");
	}
	
	public static String generateInputVnfZte(String name, String id, String vendor, String vnfd_version, String AZone, String vnf_name, String vim_name, String ip_plan) throws IOException {
		Map<String, String> model = new HashMap<String, String>();
		model.put("name", name);
		model.put("id", StringUtils.isEmpty(id) ? NEED_VALUE : id);
		model.put("AZone", StringUtils.isEmpty(AZone) ? NEED_VALUE : AZone);
		model.put("vnf_name", StringUtils.isEmpty(vnf_name) ? NEED_VALUE : vnf_name);
		model.put("vim_name", StringUtils.isEmpty(vim_name) ? NEED_VALUE : vim_name);
		model.put("vendor", StringUtils.isEmpty(vendor) ? "HUAWEI" : vendor);
		model.put("vnfd_version", StringUtils.isEmpty(vnfd_version) ? NEED_VALUE : vnfd_version);
		model.put("ip_plan", StringUtils.isEmpty(ip_plan) ? "{}" : ip_plan.trim());
		return _loadFromFile(model, "/input_vnf_zte.txt");
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

	public static String generateInputPredefineNetwork(String name) throws IOException {
		Map<String, String> model = new HashMap<String, String>();
		model.put("name", name);
		return _loadFromFile(model, "/input_predefine_network.txt");
	}

	public static String generateInputInternalNetwork(String name, String cidr, String dhcp) throws IOException {
		Map<String, String> model = new HashMap<String, String>();
		model.put("name", name);
		model.put("cidr", StringUtils.isEmpty(cidr) ? NEED_VALUE : cidr);
		model.put("dhcp", BooleanUtils.toBoolean(dhcp) ? "true" : "false");
		return _loadFromFile(model, "/input_internal_network.txt");
	}

	public static String generateInputNuage(String enterprise_name, String l2domain_template, String tosca_reference_only, String gateway, String personality, String port_name, String port_type, String physical_name) throws IOException {
		Map<String, String> model = new HashMap<String, String>();
		model.put("enterprise_name", StringUtils.isEmpty(enterprise_name) ? NEED_VALUE : enterprise_name);
		model.put("l2domain_template", StringUtils.isEmpty(l2domain_template) ? NEED_VALUE : l2domain_template);
		model.put("tosca_reference_only", BooleanUtils.toBoolean(tosca_reference_only) ? "true" : "false");
		model.put("gateway", StringUtils.isEmpty(gateway) ? NEED_VALUE : gateway);
		model.put("personality", StringUtils.isEmpty(personality) ? NEED_VALUE : personality);
		model.put("port_name", StringUtils.isEmpty(port_name) ? NEED_VALUE : port_name);
		model.put("port_type", StringUtils.isEmpty(port_type) ? NEED_VALUE : port_type);
		model.put("physical_name", StringUtils.isEmpty(physical_name) ? NEED_VALUE : physical_name);
		return _loadFromFile(model, "/input_nuage.txt");
	}

	public static String generateVnfHuawei(String vnfName, String connection_point, String requirement) throws IOException {
		Map<String, String> model = new HashMap<String, String>();
		model.put("name", vnfName);
		model.put("connection_point", connection_point);
		model.put("requirement", requirement);
		return _loadFromFile(model, "/node_template_vnf_huawei.txt");
	}
	
	public static String generateVnfZte(String vnfName, String connection_point, String requirement) throws IOException {
		Map<String, String> model = new HashMap<String, String>();
		model.put("name", vnfName);
		model.put("connection_point", connection_point);
		model.put("requirement", requirement);
		return _loadFromFile(model, "/node_template_vnf_zte.txt");
	}

	public static String generateVnfHuaweiConnectinPoint(String cp_name, String nw_name, String nw_type, String attribute_id) throws IOException {
		Map<String, String> model = new HashMap<String, String>();
		model.put("cp_name", cp_name);
		model.put("nw_name", nw_name);
		if ("predefine".equalsIgnoreCase(nw_type)) {
			return _loadFromFile(model, "/node_template_vnf_huawei_connection_point_predefine.txt");
		}
		model.put("attribute_id", attribute_id);
		return _loadFromFile(model, "/node_template_vnf_huawei_connection_point.txt");
	}
	
	public static String generateVnfZteConnectinPoint(String cp_name, String nw_name, String nw_type, String attribute_id) throws IOException {
		Map<String, String> model = new HashMap<String, String>();
		model.put("cp_name", cp_name);
		model.put("nw_name", nw_name);
		if ("predefine".equalsIgnoreCase(nw_type)) {
			return _loadFromFile(model, "/node_template_vnf_zte_connection_point_predefine.txt");
		}
		model.put("attribute_id", attribute_id);
		return _loadFromFile(model, "/node_template_vnf_zte_connection_point.txt");
	}

	public static String generateVnfRequirementVL(String cp_name, String nw_name) throws IOException {
		Map<String, String> model = new HashMap<String, String>();
		model.put("cp_name", cp_name);
		model.put("nw_name", nw_name);
		return _loadFromFile(model, "/node_template_vnf_requirement_vl.txt");
	}

	public static String generateVnfRequirementSubnet(String cp_name, String nw_name, String nw_type) throws IOException {
		Map<String, String> model = new HashMap<String, String>();
		model.put("cp_name", cp_name);
		model.put("nw_name", nw_name);
		
		if ("predefine".equalsIgnoreCase(nw_type)) {
			return _loadFromFile(model, "/node_template_vnf_requirement_predefine.txt");
		}
		return _loadFromFile(model, "/node_template_vnf_requirement_subnet.txt");
	}
	
	public static String generateVnfRequirementDepend(String vnfName) throws IOException {
		Map<String, String> model = new HashMap<String, String>();
		model.put("name", vnfName);
		return _loadFromFile(model, "/node_template_vnf_requirement_depend.txt");
	}

	public static String generateVnfVL(String nw_name) throws IOException {
		Map<String, String> model = new HashMap<String, String>();
		model.put("nw_name", nw_name);
		return _loadFromFile(model, "/node_template_vnf_vl.txt");
	}
	
	public static String generateImplModelPredefineNetwork(String nw_name) throws IOException {
		Map<String, String> model = new HashMap<String, String>();
		model.put("nw_name", nw_name);
		return _loadFromFile(model, "/implementation_model_network_predefine.txt");
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

	public static String generateImplModelNuage(String nuage_placement) throws IOException {
		Map<String, String> model = new HashMap<String, String>();
		model.put("nuage_placement", nuage_placement);
		return _loadFromFile(model, "/implementation_model_nuage.txt");
	}

	public static String generateImplModelNuageExternalNetwork(String nw_name) throws IOException {
		Map<String, String> model = new HashMap<String, String>();
		model.put("nw_name", nw_name);
		return _loadFromFile(model, "/implementation_model_network_external_nuage.txt");
	}
	
	public static String generateImplModelNuageInternalNetwork(String nw_name) throws IOException {
		Map<String, String> model = new HashMap<String, String>();
		model.put("nw_name", nw_name);
		return _loadFromFile(model, "/implementation_mode_netwrok_internal_nuage.txt");
	}

}
