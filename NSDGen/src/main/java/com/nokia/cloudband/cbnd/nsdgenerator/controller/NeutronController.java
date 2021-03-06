package com.nokia.cloudband.cbnd.nsdgenerator.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nokia.cloudband.cbnd.nsdgenerator.util.TemplateUtil;

@RestController
@RequestMapping(path = "/neutron")
public class NeutronController extends GenericController {

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
		return "external_id";
	}

	@Override
	protected String generateBasicImplementationModel(Map<String, String> nuageObj, List<String> placements) throws IOException {
		return "";
	}
	
	@Override
	protected String generateNuageInput(Map<String, String> nuageObj) {
		return "";
	}

	@Override
	protected String generateImplModelExternalNetwork(Map<String, String> networkData, List<String> placements) throws IOException {
		String nw_ext_name = networkData.get("nw_ext_name");
		String nw_ext_type = networkData.get("nw_ext_type");
		placements.add(nw_ext_name + "_net");
		return TemplateUtil.generateImplModelNuetronExternalNetwork(nw_ext_name, nw_ext_type);
	}
	
	@Override
	protected String generateImplModelInternalNetwork(Map<String, String> networkData, List<String> placements) throws IOException {
		String nw_int_name = networkData.get("nw_int_name");
		placements.add(nw_int_name + "_net");
		return TemplateUtil.generateImplModelNuetronInternalNetwork(nw_int_name);
	}

	
	

}
