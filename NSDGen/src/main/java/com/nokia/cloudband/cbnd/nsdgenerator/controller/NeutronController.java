package com.nokia.cloudband.cbnd.nsdgenerator.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nokia.cloudband.cbnd.nsdgenerator.util.TemplateUtil;
import com.nokia.cloudband.cbnd.nsdgenerator.util.ZipUtil;

@RestController
@RequestMapping(path = "/neutron")
public class NeutronController {

	private Log log = LogFactory.getLog(getClass());

	private Map<String, String> convertToMap(String arrayJson) throws JsonParseException, JsonMappingException, IOException {
		ArrayList<Map<String, String>> objs = new ObjectMapper().readValue(arrayJson, ArrayList.class);
		Map<String, String> results = new HashMap<String, String>();
		for (int i = 0; i < objs.size(); i++) {
			Map<String, String> object = objs.get(i);
			results.put(object.get("name"), object.get("value"));
		}
		return results;
	}

	@RequestMapping(path = "/download", method = RequestMethod.GET, produces = "application/zip")
	public void download(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession();
		
		response.setContentType("application/zip");
		response.setStatus(HttpServletResponse.SC_OK);
		response.addHeader("Content-Disposition", "attachment; filename=\"" + (String) session.getAttribute("mainYamlFileName") + ".zip\"");

		File zipFile = new File((String) session.getAttribute("file"));
		
		FileInputStream in = null;
		try {
			in = new FileInputStream(zipFile);
			IOUtils.copy(in, response.getOutputStream());
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	@RequestMapping(path = "/generate", method = RequestMethod.POST)
	public ResponseEntity<?> generate(@RequestBody String request, HttpSession session) {
		try {
			Map<String, String> requestObj = new ObjectMapper().readValue(request, HashMap.class);

			Map<String, String> metadataObj = convertToMap(requestObj.get("metadata"));

			ArrayList<Map<String, String>> networkObjList = new ObjectMapper().readValue(requestObj.get("networks"), ArrayList.class);

			Map<String, String> vnfObj = convertToMap(requestObj.get("vnf"));

			List<String> placements = new ArrayList<String>();

			// prepare yaml folder
			String metaName = metadataObj.get("meta_name");
			String metaVersion = metadataObj.get("meta_version");

			String mainYamlFileName = metaName + "_" + metaVersion + ".yaml";

			// create metadata file
			String metadataContent = TemplateUtil.generateMetadata(metaName, metaVersion, metadataObj.get("description"), mainYamlFileName);

			// prepare input param
			// vnf
			String vnfInput = "";
			if ("huawei".equalsIgnoreCase(vnfObj.get("vnf_type"))) {
				String vnfm_h_name = vnfObj.get("vnfm_h_name");
				String vnfm_h_id = vnfObj.get("vnfm_h_id");
				String vnfm_h_vnfd_id_in_blueprint = vnfObj.get("vnfm_h_vnfd_id_in_blueprint");
				String vnfm_h_plan_name = vnfObj.get("vnfm_h_plan_name");
				String vnfm_h_vendor = vnfObj.get("vnfm_h_vendor");
				String vnfm_h_vnfd_version = vnfObj.get("vnfm_h_vnfd_version");
				vnfInput = TemplateUtil.generateInputVnfHuawei(vnfm_h_name, vnfm_h_id, vnfm_h_vnfd_id_in_blueprint, vnfm_h_plan_name, vnfm_h_vendor, vnfm_h_vnfd_version);

			} else if ("zte".equalsIgnoreCase(vnfObj.get("vnf_type"))) {
				throw new RuntimeException("zte noti imple");
			} else {
				throw new RuntimeException("cbam noti imple");
			}
			// network
			String networkInput = "";
			for (Map<String, String> network : networkObjList) {
				Map<String, String> networkData = convertToMap(network.get("value"));
				String nw_type = networkData.get("nw_type");
				if ("external".equalsIgnoreCase(nw_type)) {
					String nw_ext_name = networkData.get("nw_ext_name");
					String nw_ext_cidr = networkData.get("nw_ext_cidr");
					String nw_ext_dhcp = networkData.get("nw_ext_dhcp");
					String nw_ext_gateway = networkData.get("nw_ext_gateway");
					String nw_ext_vlan = networkData.get("nw_ext_vlan");
					String nw_ext_phys = networkData.get("nw_ext_phys");
					String nw_ext_type = networkData.get("nw_ext_type");

					networkInput += TemplateUtil.generateInputExternalNeutron(nw_ext_name, nw_ext_cidr, nw_ext_gateway, nw_ext_dhcp, nw_ext_vlan, nw_ext_phys, nw_ext_type);

				} else if ("internal".equalsIgnoreCase(nw_type)) {
					String nw_int_name = networkData.get("nw_int_name");
					String nw_int_cidr = networkData.get("nw_int_cidr");
					String nw_int_dhcp = networkData.get("nw_int_dhcp");

					networkInput += TemplateUtil.generateInputInternalNeutron(nw_int_name, nw_int_cidr, nw_int_dhcp);

				} else {
					// predefine
					String nw_p_name = networkData.get("nw_p_name");
					String nw_p_uuid = networkData.get("nw_p_uuid");

					networkInput += TemplateUtil.generateInputPredefineNeutron(nw_p_name, nw_p_uuid);

				}
			}

			String input = vnfInput + networkInput;

			// node_template
			String node_template = "";
			String connection_point = "";
			String requirement = "";
			String virtual_link = "";
			String vnf_name = "";
			if ("huawei".equalsIgnoreCase(vnfObj.get("vnf_type"))) {
				vnf_name = vnfObj.get("vnfm_h_name");

				int index = 0;

				while (vnfObj.containsKey("connection_point_name" + index)) {
					String cp_name = vnfObj.get("connection_point_name" + index);
					String nw_name = vnfObj.get("connection_point_mapping" + index);

					index++;
					String nw_type = null;
					for (Map<String, String> network : networkObjList) {
						Map<String, String> networkData = convertToMap(network.get("value"));
						String toFindNwName;
						if ("external".equalsIgnoreCase(nw_type)) {
							toFindNwName = networkData.get("nw_ext_name");
						} else if ("internal".equalsIgnoreCase(nw_type)) {
							toFindNwName = networkData.get("nw_int_name");
						} else {
							toFindNwName = networkData.get("nw_p_name");
						}

						if (StringUtils.equalsIgnoreCase(nw_name, toFindNwName)) {
							nw_type = networkData.get("nw_type");
							break;
						}
					}

					connection_point += TemplateUtil.generateVnfHuaweiConnectinPoint(cp_name, nw_name, nw_type);

					requirement += TemplateUtil.generateVnfRequirementVL(cp_name, nw_name);

					requirement += TemplateUtil.generateVnfRequirementSubnet(nw_name);

				}

				placements.add(vnf_name + "_vnf");

			} else if ("zte".equalsIgnoreCase(vnfObj.get("vnf_type"))) {
				throw new RuntimeException("zte not imple");
			} else {
				throw new RuntimeException("cbam not imple");
			}

			// implementation_model
			String implementation_model = "";
			for (Map<String, String> network : networkObjList) {
				Map<String, String> networkData = convertToMap(network.get("value"));
				String nw_type = networkData.get("nw_type");
				if ("external".equalsIgnoreCase(nw_type)) {
					String nw_ext_name = networkData.get("nw_ext_name");
					String nw_ext_type = networkData.get("nw_ext_type");

					implementation_model += TemplateUtil.generateImplModelNuetronExternalNetwork(nw_ext_name, nw_ext_type);
					
					virtual_link += TemplateUtil.generateVnfVL(nw_ext_name);

					placements.add(nw_ext_name + "_impl");

				} else if ("internal".equalsIgnoreCase(nw_type)) {
					String nw_int_name = networkData.get("nw_int_name");

					implementation_model += TemplateUtil.generateImplModelNuetronInternalNetwork(nw_int_name);
					
					virtual_link += TemplateUtil.generateVnfVL(nw_int_name);

					placements.add(nw_int_name + "_impl");

				} else {
					// no implementation model for predefine
				}
			}
			
			node_template = TemplateUtil.generateVnfHuawei(vnf_name, connection_point, requirement, virtual_link);

			String mainYamlContent = TemplateUtil.generateYaml(input, node_template, implementation_model, placements.toString());

			// make zip
			File zipFile = File.createTempFile("ZIP", "1");
			session.setAttribute("mainYamlFileName", mainYamlFileName);
			session.setAttribute("file", zipFile.getAbsolutePath());

			ZipUtil.makeZip(zipFile, metadataContent, mainYamlFileName, mainYamlContent);

		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ResponseEntity<Void>(HttpStatus.OK);
	}
}
