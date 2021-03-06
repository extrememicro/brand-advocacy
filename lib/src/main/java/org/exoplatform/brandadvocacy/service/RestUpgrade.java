package org.exoplatform.brandadvocacy.service;

import org.exoplatform.brandadvocacy.jcr.MissionParticipantNoteDAO;
import org.exoplatform.brandadvocacy.model.MissionParticipant;
import org.exoplatform.brandadvocacy.model.Program;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.jcr.core.nodetype.NodeDefinitionValue;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeValue;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitionValue;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.impl.RuntimeDelegateImpl;
import org.exoplatform.services.rest.resource.ResourceContainer;

import javax.annotation.security.RolesAllowed;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.version.OnParentVersionAction;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by exoplatform on 23/12/14.
 */
@Path("brandadv/")
public class RestUpgrade implements ResourceContainer {

  private Log log = ExoLogger.getLogger(this.getClass());
  private IService iService;
  private static final String[] childNodePaths = new String[] {

  };
  protected static final CacheControl cacheControl_;
  static {
    RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
    cacheControl_ = new CacheControl();
    cacheControl_.setNoCache(true);
    cacheControl_.setNoStore(true);
  }

  public RestUpgrade(IService iService){
    this.iService = iService;
  }

  @GET
  @Path("/upgrade/nodetype/{name}/{children}")
  @RolesAllowed({"administrators"})
  public Response upgradeNodeTypeChild(@PathParam("name") String nodeTypeName,@PathParam("children") String childNode){
    String result = "upgrade node type successfully";
    try {
      log.info("==== start upgrade brand adv node type "+nodeTypeName);
      ExtendedNodeTypeManager nodeTypeManager =   CommonsUtils.getRepository().getNodeTypeManager();
      NodeTypeValue nodeTypeValue = nodeTypeManager.getNodeTypeValue(nodeTypeName);
      List<NodeDefinitionValue> childValues = nodeTypeValue.getDeclaredChildNodeDefinitionValues();
      if (childValues.size() == 0){
        log.info("==== add child node "+childNode+" to node type "+nodeTypeName);
        List<NodeDefinitionValue> nodeDefinitionValues = new ArrayList<NodeDefinitionValue>();
        NodeDefinitionValue noteslist = new NodeDefinitionValue();
        noteslist.setName(childNode);
        List<String> requiredPrimaryTypes = new ArrayList<String>();
        requiredPrimaryTypes.add("brad:noteslist");
        noteslist.setRequiredNodeTypeNames(requiredPrimaryTypes);
        noteslist.setDefaultNodeTypeName("brad:noteslist");
        noteslist.setOnVersion(OnParentVersionAction.COPY);
        noteslist.setSameNameSiblings(false);
        noteslist.setMandatory(false);
        noteslist.setAutoCreate(true);
        nodeDefinitionValues.add(noteslist);

        nodeTypeValue.setDeclaredChildNodeDefinitionValues(nodeDefinitionValues);
      }
      nodeTypeManager.registerNodeType(nodeTypeValue, ExtendedNodeTypeManager.REPLACE_IF_EXISTS);
    } catch (Exception e) {
      if (log.isErrorEnabled()) {
        log.error("An unexpected error occurs when migrating exo:actionable node type", e);
      }
      result = "upgrade node type unsuccessful";
    }
    return Response.ok(result , MediaType.TEXT_HTML).cacheControl(cacheControl_).build();
  }
  @GET
  @Path("/upgrade/nodetype/{name}/{property}/{type}")
  @RolesAllowed({"administrators"})
  public Response upgradeNodeTypeProperty(@PathParam("name") String nodeTypeName,@PathParam("property") String propertyName,@PathParam("type") String type){
    String result = "upgrade node type successfully";
    log.info("==== start upgrade brand adv node type "+nodeTypeName);
    try {
      ExtendedNodeTypeManager nodeTypeManager =   CommonsUtils.getRepository().getNodeTypeManager();
      NodeTypeValue nodeTypeValue = nodeTypeManager.getNodeTypeValue(nodeTypeName);
      List<PropertyDefinitionValue> propValues = nodeTypeValue.getDeclaredPropertyDefinitionValues();
      for (PropertyDefinitionValue propValue : propValues) {
        if (propertyName.equalsIgnoreCase(propValue.getName())) {
          log.info("==== update property "+propertyName+" set to type "+type);
          if(type == "long")
            propValue.setRequiredType(PropertyType.LONG);
          break;
        }
      }
      nodeTypeValue.setDeclaredPropertyDefinitionValues(propValues);
      nodeTypeManager.registerNodeType(nodeTypeValue, ExtendedNodeTypeManager.REPLACE_IF_EXISTS);
    }
    catch (Exception e) {
      if (log.isErrorEnabled()) {
        log.error("An unexpected error occurs when migrating exo:actionable node type", e);
      }
    }
    return Response.ok(result, MediaType.TEXT_HTML).cacheControl(cacheControl_).build();
  }

  @GET
  @Path("/upgrade/childnode/data")
  @RolesAllowed({"administrators"})
  public Response upgradeChildnodeData(){
    String result = "upgrade child node for data successfully";
    List<Program> programs = iService.getAllPrograms();
    for (Program program:programs){
      List<MissionParticipant> missionParticipants = iService.getAllMissionParticipantsInProgram(program.getId());
      for (MissionParticipant missionParticipant:missionParticipants){
        if(!iService.initMPHomeNote(missionParticipant.getId()))
          result = "upgrade child node for data unsuccessfully";
      }
    }
    return Response.ok(result, MediaType.TEXT_HTML).cacheControl(cacheControl_).build();
  }

}
