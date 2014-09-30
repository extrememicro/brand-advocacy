/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.brandadvocacy.jcr;

import org.exoplatform.brandadvocacy.model.*;
import org.exoplatform.brandadvocacy.service.BrandAdvocacyServiceException;
import org.exoplatform.brandadvocacy.service.JCRImpl;
import org.exoplatform.brandadvocacy.service.Utils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import java.util.*;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Sep 9, 2014  
 */
public class MissionParticipantDAO extends DAO {

  private static final Log log = ExoLogger.getLogger(MissionParticipantDAO.class);

  public static final String node_prop_labelID = "exo:labelID";
  public static final String node_prop_mission_id = "exo:mission_id";
  public static final String node_prop_proposition_id = "exo:proposition_id";
  public static final String node_prop_participant_username = "exo:participant_username";
  public static final String node_prop_url_submitted = "exo:url_submitted";
  public static final String node_prop_address_id = "exo:address_id";
  public static final String node_prop_size = "exo:size";
  public static final String node_prop_status = "exo:status";
  public static final String node_prop_dateCreated = "exo:dateCreated";
  public static final String node_prop_modifiedDate = "exo:modifiedDate";

  public MissionParticipantDAO(JCRImpl jcrImpl) {
    super(jcrImpl);
  }
  private void setProperties(Node aNode,MissionParticipant missionParticipant) throws RepositoryException {
    aNode.setProperty(node_prop_labelID,missionParticipant.getLabelID());
    aNode.setProperty(node_prop_mission_id,missionParticipant.getMission_id());
    aNode.setProperty(node_prop_proposition_id, missionParticipant.getProposition_id());
    aNode.setProperty(node_prop_participant_username,missionParticipant.getParticipant_username());
    aNode.setProperty(node_prop_url_submitted,missionParticipant.getUrl_submitted());
    aNode.setProperty(node_prop_address_id,missionParticipant.getAddress_id());
    aNode.setProperty(node_prop_size,missionParticipant.getSize().getValue());
    aNode.setProperty(node_prop_status,missionParticipant.getStatus().getValue());
    aNode.setProperty(node_prop_dateCreated,missionParticipant.getCreatedDate());
    aNode.setProperty(node_prop_modifiedDate,missionParticipant.getModifiedDate());
  }
  private MissionParticipant transferNode2Object(Node node) throws RepositoryException{
    MissionParticipant missionParticipant = new MissionParticipant();
    missionParticipant.setId(node.getUUID());
    PropertyIterator iter = node.getProperties("exo:*");
    while (iter.hasNext()) {
      Property p = (Property) iter.next();
      String name = p.getName();
      if (name.equals(node_prop_labelID)){
        missionParticipant.setLabelID(p.getString());
      }
      else if(name.equals(node_prop_mission_id)){
        missionParticipant.setMission_id(p.getString());
      } else if(name.equals(node_prop_mission_id)){
        missionParticipant.setMission_id(p.getString());
      } else if(name.equals(node_prop_proposition_id)){
        missionParticipant.setProposition_id(p.getString());
      } else if (name.equals(node_prop_participant_username)){
        missionParticipant.setParticipant_username(p.getString());
      } else if (name.equals(node_prop_url_submitted)){
        missionParticipant.setUrl_submitted(p.getString());
      } else if (name.equals(node_prop_address_id)){
        missionParticipant.setAddress_id(p.getString());
      } else if (name.equals(node_prop_size)){
        missionParticipant.setSize(Size.getSize((int) p.getLong()));
      } else if (name.equals(node_prop_status)){
        missionParticipant.setStatus(Status.getStatus((int) p.getLong()));
      } else if (name.equals(node_prop_dateCreated)){
        missionParticipant.setCreatedDate(p.getLong());
      } else if(name.equals(node_prop_modifiedDate)){
        missionParticipant.setModifiedDate(p.getLong());
      }
    }
    return missionParticipant;
  }
  private List<MissionParticipant> transferNodes2Objects(List<Node> nodes){
    List<MissionParticipant> missionParticipants = new ArrayList<MissionParticipant>();
    MissionParticipant missionParticipant;
    for (Node node:nodes){
      try {
        missionParticipant = this.transferNode2Object(node);
        missionParticipant.checkValid();
        missionParticipants.add(missionParticipant);
      } catch (RepositoryException e) {
        e.printStackTrace();
      }
    }
    return missionParticipants;
  }
  public Node getOrCreateMissionParticipantHome() {
    String path = String.format("%s/%s",JCRImpl.EXTENSION_PATH,JCRImpl.MISSION_PARTICIPANT_PATH);
    return this.getJcrImplService().getOrCreateNode(path);
  }
  public Node getNodeById(String id) throws RepositoryException{
    return this.getJcrImplService().getSession().getNodeByUUID(id);
  }
  public Node getMissionParticipantNodeByLabelID(String labelID){
    StringBuilder sql = new StringBuilder("select * from "+ JCRImpl.MISSION_PARTICIPANT_NODE_TYPE +" where jcr:path like '");
    sql.append(JCRImpl.EXTENSION_PATH).append("/").append(JCRImpl.MISSION_PARTICIPANT_PATH);
    sql.append("/").append(Utils.queryEscape(labelID));
    sql.append("'");
    Session session;
    try {
      session = this.getJcrImplService().getSession();
      Query query = session.getWorkspace().getQueryManager().createQuery(sql.toString(), Query.SQL);
      QueryResult result = query.execute();
      NodeIterator nodes = result.getNodes();
      if (nodes.hasNext()) {
        return nodes.nextNode();
      }
    } catch (RepositoryException e) {
      log.error("ERROR cannot get mission participant node  "+ labelID +" Exception "+e.getMessage());
    }
    return null;
  }
  public List<MissionParticipant> searchMissionParticipants(String keyword){
    StringBuilder sql = new StringBuilder("select * from "+ JCRImpl.MISSION_PARTICIPANT_NODE_TYPE +" where ");
    sql.append(node_prop_labelID).append(" like '%"+keyword+"%'");
    sql.append(" OR "+node_prop_participant_username).append(" like '%"+keyword+"%'");
    List<Node> nodes =  this.getNodesByQuery(sql.toString());
    return this.transferNodes2Objects(nodes);
  }

  public MissionParticipant addMissionParticipant(MissionParticipant missionParticipant){
    try{
      missionParticipant.checkValid();
      Node missionParticipantHome = this.getOrCreateMissionParticipantHome();
      if (null != missionParticipantHome){
        Node missionParticipantNode =  missionParticipantHome.addNode(missionParticipant.getLabelID(),JCRImpl.MISSION_PARTICIPANT_NODE_TYPE);
        this.setProperties(missionParticipantNode,missionParticipant);
        missionParticipantHome.getSession().save();
        return this.transferNode2Object(missionParticipantNode);
      }
    }
    catch (ItemExistsException ie){
      log.error(" === ERROR cannot add existing item "+ie.getMessage());
    }
    catch (RepositoryException e) {
      log.error("=== ERROR cannot add mission participant in "+missionParticipant.getMission_id()+" - Exception "+e.getMessage());
    }
    catch (BrandAdvocacyServiceException brade){
      log.error(brade.getMessage());
    }
    return null;
  }
  public MissionParticipant updateMissionParticipant(MissionParticipant missionParticipant){
    try {

      missionParticipant.checkValid();
      Node missionParticipantHome = this.getOrCreateMissionParticipantHome();
      if (null != missionParticipantHome && missionParticipantHome.hasNode(missionParticipant.getLabelID())){
        Node missionParticipantNode = missionParticipantHome.getNode(missionParticipant.getLabelID());
        if(null != missionParticipantNode && missionParticipant.getId().equals(missionParticipantNode.getUUID()) ){
          this.setProperties(missionParticipantNode,missionParticipant);
           missionParticipantNode.save();
          return this.transferNode2Object(missionParticipantNode);
        }else
          throw new BrandAdvocacyServiceException(BrandAdvocacyServiceException.MISSION_PARTICIPANT_NOT_EXISTS," cannot update mission participant not exists");
      }
    }
    catch (RepositoryException e) {
      log.error("=== ERROR cannot add mission participant in "+missionParticipant.getMission_id()+" - Exception "+e.getMessage());
    }
    catch (BrandAdvocacyServiceException brade){
      log.error(brade.getMessage());
    }
    return null;
  }

  public List<MissionParticipant> getAllMissionParticipants(){

    Set<MissionParticipant> missionParticipants = new HashSet<MissionParticipant>();
    try{
      Node missionParticipantHome = this.getOrCreateMissionParticipantHome();
      NodeIterator nodes =  missionParticipantHome.getNodes();
      while (nodes.hasNext()){
        try{
          missionParticipants.add(this.transferNode2Object(nodes.nextNode()));
        }catch (RepositoryException re){
          log.error(" === ERROR cannot get mission participant node list ");
        }
      }
      List<MissionParticipant> result = new LinkedList<MissionParticipant>();
      result.addAll(missionParticipants);
      Collections.sort(result,new Comparator<MissionParticipant>() {
        @Override
        public int compare(MissionParticipant o1, MissionParticipant o2) {
          return (int)(o2.getCreatedDate() - o1.getCreatedDate());
        }
      });
      return result;
    }catch (RepositoryException re){
      log.error("=== ERROR cannot find all mission participants");
    }
    return null;

  }
  public List<MissionParticipant> getAllMissionParticipantsByParticipant(String username){

    List<MissionParticipant> missionParticipants = new ArrayList<MissionParticipant>();
    ParticipantDAO participantDAO = this.getJcrImplService().getParticipantDAO();
    Node participantNode = participantDAO.getNodeByUserName(username);
    if (null != participantNode){
      Participant participant = null;
      try {
        participant = participantDAO.transferNode2Object(participantNode);
        Set<String> mpids = participant.getMission_participant_ids();
        Session session = this.getJcrImplService().getSession();
        MissionParticipant missionParticipant;
        for (String mpid:mpids){
          missionParticipant = this.transferNode2Object(session.getNodeByUUID(mpid));
          missionParticipant.checkValid();
          missionParticipants.add(missionParticipant);
        }
      } catch (RepositoryException e) {
        log.error("=== ERROR getAllMissionParticipantsByParticipant: cannot transfer node to object "+username);
        e.printStackTrace();
      }

    }
    return missionParticipants;
  }

}