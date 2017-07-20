package com.nokia.cloudband.cbnd.nsdgenerator.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nokia.cloudband.cbnd.nsdgenerator.util.TemplateUtil;

@RestController
@RequestMapping(path = "/nuage")
public class NuageController extends GenericController {

	@Override
	protected String generateInputExternalNetwork(Map<String, String> networkData) throws IOException {
		String nw_ext_name = networkData.get("nw_ext_name");
		String nw_ext_cidr = networkData.get("nw_ext_cidr");
		String nw_ext_dhcp = networkData.get("nw_ext_dhcp");
		String nw_ext_gateway = networkData.get("nw_ext_gateway");
		String nw_ext_vlan = networkData.get("nw_ext_vlan");
		String nw_ext_phys = networkData.get("nw_ext_phys");
		String nw_ext_type = networkData.get("nw_ext_type");

		return TemplateUtil.generateInputExternalNeutron(nw_ext_name, nw_ext_cidr, nw_ext_gateway, nw_ext_dhcp, nw_ext_vlan, nw_ext_phys, nw_ext_type);
	}
	
	@Override
	protected String getNetworkAttributeId() throws IOException {
		return "ID";
	}

	@Override
	protected String generateBasicImplementationModel(Map<String, String> nuageObj, List<String> placements) throws IOException {
		placements.add("nuage_placement");
		return TemplateUtil.generateImplModelNuage("nuage_placement");
	}

	@Override
	protected String generateNuageInput(Map<String, String> nuageObj) throws IOException {
		String enterprise_name = nuageObj.get("enterprise_name");
		String l2domain_template = nuageObj.get("l2domain_template");
		String tosca_reference_only = nuageObj.get("tosca_reference_only");
		String gateway = nuageObj.get("gateway");
		String personality = nuageObj.get("personality");
		String port_name = nuageObj.get("port_name");
		String port_type = nuageObj.get("port_type");
		String physical_name = nuageObj.get("physical_name");
		return TemplateUtil.generateInputNuage(enterprise_name, l2domain_template, tosca_reference_only, gateway, personality, port_name, port_type, physical_name);
	}

	

	@Override
	protected String generateImplModelExternalNetwork(Map<String, String> networkData, List<String> placements) throws IOException {
		String nw_ext_name = networkData.get("nw_ext_name");
		return TemplateUtil.generateImplModelNuageExternalNetwork(nw_ext_name);
	}

	@Override
	protected String generateImplModelInternalNetwork(Map<String, String> networkData, List<String> placements) throws IOException {
		String nw_int_name = networkData.get("nw_int_name");
		return TemplateUtil.generateImplModelNuageInternalNetwork(nw_int_name);
	}
	

}
