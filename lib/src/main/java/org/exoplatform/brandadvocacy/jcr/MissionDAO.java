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

import org.exoplatform.brandadvocacy.model.Mission;
import org.exoplatform.brandadvocacy.model.Participant;
import org.exoplatform.brandadvocacy.model.Priority;
import org.exoplatform.brandadvocacy.service.BrandAdvocacyServiceException;
import org.exoplatform.brandadvocacy.service.JCRImpl;
import org.exoplatform.brandadvocacy.service.Utils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Sep
 * 9, 2014
 */
public class MissionDAO extends DAO {

  private static final Log   log             = ExoLogger.getLogger(MissionDAO.class);

  public static final String MISSIONS_PATH   = "Missions";

  public static final String node_prop_labelID = "exo:labelID";
  public static final String node_prop_title = "exo:title";  
  public static final String node_prop_third_party_link = "exo:third_party_link";
  public static final String node_prop_priority = "exo:priority";
  public static final String node_prop_active = "exo:active";
  public static final String node_prop_dateCreated = "exo:dateCreated";
  public static final String node_prop_modifiedDate = "exo:modifiedDate";
  public static final String node_prop_managers = "exo:managerslist";
  public static final String node_prop_propositions = "exo:propositionslist";
  
  public MissionDAO(JCRImpl jcrImpl) {
    super(jcrImpl);


  }

  public Node getOrCreateMissionHome() {
    String path = String.format("%s/%s",JCRImpl.EXTENSION_PATH,MISSIONS_PATH);
    return this.getJcrImplService().getOrCreateNode(path);
  }
  public Node getOrCreateManagerHome(Node missionNode) throws RepositoryException {

    Node managerHome = null;
    try {
      managerHome = missionNode.getNode(node_prop_managers);
    } catch (RepositoryException e) {
      log.error("managers list node not exists");
    }
    if(null == managerHome){
      try {
        managerHome = missionNode.addNode(node_prop_managers,JCRImpl.MANAGER_LIST_NODE_TYPE);
      } catch (RepositoryException e) {
        e.printStackTrace();
      }
    }
    return managerHome;
  }

  public Node getOrCreatePropositionHome(Node missionNode) throws RepositoryException {

    Node propostionHome = null;
    try {
      propostionHome = missionNode.getNode(node_prop_propositions);
    } catch (RepositoryException e) {
      log.error("prositions list node not exists");
    }
    if(null == propostionHome){
      try {
        propostionHome = missionNode.addNode(node_prop_propositions,JCRImpl.PROPOSITION_LIST_NODE_TYPE);
      } catch (RepositoryException e) {
        e.printStackTrace();
      }
    }
    return propostionHome;
  }

  public void setPropertiesNode(Node missionNode, Mission m) throws RepositoryException {
    if(null != m.getLabelID() && !"".equals(m.getLabelID()))
      missionNode.setProperty(node_prop_labelID,m.getLabelID());
    missionNode.setProperty(node_prop_title, m.getTitle());
    missionNode.setProperty(node_prop_third_party_link, m.getThird_party_link());
    missionNode.setProperty(node_prop_priority, m.getPriority().getValue());
    missionNode.setProperty(node_prop_active, m.getActive());
    if (0 != m.getCreatedDate())
      missionNode.setProperty(node_prop_dateCreated, m.getCreatedDate());
    if (0 != m.getModifiedDate())
      missionNode.setProperty(node_prop_modifiedDate,m.getModifiedDate());
  }
  public Mission transferNode2Object(Node node) throws RepositoryException {
    Mission m = new Mission();
    m.setId(node.getUUID());
    PropertyIterator iter = node.getProperties("exo:*");
    iter =  node.getProperties();
    Property p;
    String name;
    while (iter.hasNext()) {
      p = iter.nextProperty();
      name = p.getName();
      if (name.equals(node_prop_labelID)) {
        m.setLabelID(p.getString());
      } else if (name.equals(node_prop_title)) {
        m.setTitle(p.getString());
      } else if (name.equals(node_prop_third_party_link)) {
        m.setThird_party_link(p.getString());
      } else if(name.equals(node_prop_priority)){
        m.setPriority(Priority.getPriority((int) p.getLong()));
      } else if(name.equals(node_prop_active)){
        m.setActive(p.getBoolean());
      } else if (name.equals(node_prop_dateCreated)) {
        m.setCreatedDate(p.getLong());
      }
    }
    try {
      m.checkValid();
      return m;
    }
    catch (BrandAdvocacyServiceException brade){
      log.error(" ERROR cannot tranfert node to mission object "+brade.getMessage());
    }
    return null;
  }
  public List<Mission> transferNodes2Objects(List<Node> nodes) {
    List<Mission> missions = new ArrayList<Mission>(nodes.size());
    Mission mission;
    for (Node node:nodes){
      try {
        mission = this.transferNode2Object(node);
        mission.checkValid();
        missions.add(mission);
      } catch (RepositoryException e) {
        e.printStackTrace();
      }
    }
    return missions;
  }
  public Mission createMission(Mission m) {
    try {
      m.checkValid();
      try {
        Node homeMissionNode = this.getOrCreateMissionHome();
        Node missionNode = homeMissionNode.addNode(m.getLabelID(),JCRImpl.MISSION_NODE_TYPE);
        this.setPropertiesNode(missionNode,m);
        homeMissionNode.getSession().save();
        if(null != m.getManagers() && m.getManagers().size() > 0)
          this.getJcrImplService().getManagerDAO().addManager2Mission(missionNode.getUUID(), m.getManagers());
        return this.transferNode2Object(missionNode);
      }catch (ItemExistsException ie){
        log.error(" === ERROR cannot add existing item "+ie.getMessage());
      }
      catch (RepositoryException e) {
        log.error(" repo exception "+e.getMessage());
      }
    } catch (BrandAdvocacyServiceException brade) {
      log.error("cannot create mission with null title");
    }
    return null;
  }
  public List<Mission> getAllMissions(){
    List<Mission> missions = new ArrayList<Mission>();
    Node missionHome = this.getOrCreateMissionHome();
    try {
      NodeIterator nodes =  missionHome.getNodes();
      while (nodes.hasNext()) {
        missions.add(this.transferNode2Object(nodes.nextNode()));
      }
      return missions;
    } catch (RepositoryException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }
  public Node getNodeByLabelID(String labelID){
    StringBuilder sql = new StringBuilder("select * from "+ JCRImpl.MISSION_NODE_TYPE +" where jcr:path like '");
    sql.append(JCRImpl.EXTENSION_PATH).append("/").append(MISSIONS_PATH);
    sql.append("/").append(Utils.queryEscape(labelID)).append("'");
    List<Node> nodes =  this.getNodesByQuery(sql.toString(),0,1);
    if (nodes.size() > 0) {
      return nodes.get(0);
    }
    return null;
  }
  public Node getNodeById(String id) throws RepositoryException {

    return this.getJcrImplService().getSession().getNodeByUUID(id);

  }

  public Mission getMissionById(String id) {
    Node aNode = null;
    try {
      aNode = this.getNodeById(id);
      if(aNode != null)
        return this.transferNode2Object(aNode);
    } catch (RepositoryException e) {
      log.error("ERROR cannot get mission by id "+e.getStackTrace());
    }

    return null;
  }
  public Mission updateMission(Mission m){
    try {
      m.checkValid();      
      Node missiondeNode = this.getNodeById(m.getId());
      if (null == missiondeNode) {
      
        throw new BrandAdvocacyServiceException(BrandAdvocacyServiceException.MISSION_NOT_EXISTS, "cannot update mission not exist "+m.getLabelID());
        
      }
      if (null != missiondeNode && m.getLabelID().equals( missiondeNode.getProperty(node_prop_labelID).getString()) ) {
        this.setPropertiesNode(missiondeNode, m);
        missiondeNode.save();
        return this.transferNode2Object(missiondeNode);
      }

    } catch (BrandAdvocacyServiceException e) {
      log.error("ERROR cannot update mission with empty title");
    } catch (RepositoryException e) {
      log.error("ERROR update mission "+e.getMessage());
    }
    return null;
  }
  public void removeMissionById(String id){
      try {
          Node aNode = this.getNodeById(id);
          if (aNode != null) {
              Session session = aNode.getSession();
              aNode.remove();
              session.save();
          }
      } catch (RepositoryException e) {
          log.error(" ERROR remove mission "+id+" === Exception "+e.getMessage());
      }
  }
  
  public void setActiveMission(String id,Boolean isActive){
    try{
      if(null == id || "".equals(id))
        throw new BrandAdvocacyServiceException(BrandAdvocacyServiceException.ID_INVALID,"cannot set active for invalid id "+id);
      Node aNode = this.getNodeById(id);
      if(null == aNode)
        throw new BrandAdvocacyServiceException(BrandAdvocacyServiceException.MISSION_NOT_EXISTS,"cannot set active for mission not exist "+id);
      aNode.setProperty(node_prop_active, isActive);
    }catch(BrandAdvocacyServiceException brade){
      log.error(brade.getMessage());
    }catch(RepositoryException re){
      log.error(" ERROR set active mission "+re.getMessage());
    }
  }

  public int getTotalNumberMissions(Boolean isPublic, Boolean isActive,int priority){
    StringBuilder sql = new StringBuilder("select jcr:uuid from "+ JCRImpl.MISSION_NODE_TYPE +" where ");

    if(isPublic){
      sql.append(node_prop_active).append(" = 'true' ");
    }else{
      sql.append(node_prop_active).append("='").append(isActive).append("'");
    }

    if(priority != 0){
      sql.append(" AND ").append(node_prop_priority).append("=").append(priority);
    }
    return this.getNodesByQuery(sql.toString(),0,0).size();
  }
  public Mission getRandomMission(int priority){

    int total = this.getTotalNumberMissions(true,true,priority);
    if(0 == total)
      return null;
    StringBuilder sql = new StringBuilder("select * from "+ JCRImpl.MISSION_NODE_TYPE +" where ");
    sql.append(node_prop_active).append("= 'true' ");
    sql.append(" AND ").append(node_prop_priority).append("=").append(priority);
    Random random = new Random();
    List<Node> nodes =  this.getNodesByQuery(sql.toString(), random.nextInt(total),1);
    try {
      if (nodes.size() > 0)
        return this.transferNode2Object(nodes.get(0));
    } catch (RepositoryException e) {
      log.error("ERROR cannot get random mission");
    }
    return null;
  }

  public List<Mission> getAllMissionsByParticipant(String username){

    List<Mission> missions = new ArrayList<Mission>();
    ParticipantDAO participantDAO = this.getJcrImplService().getParticipantDAO();
    Node participantNode = participantDAO.getNodeByUserName(username);
    if (null != participantNode){
      Participant participant = null;
      try {
        participant = participantDAO.transferNode2Object(participantNode);
        Set<String> mids = participant.getMission_ids();
        Mission mission;
        for (String mid:mids){
          mission = this.transferNode2Object(this.getNodeById(mid));
          mission.checkValid();
          missions.add(mission);
        }
      } catch (RepositoryException e) {
        log.error("=== ERROR getAllMissionParticipantsByParticipant: cannot transfer node to object "+username);
        e.printStackTrace();
      }

    }
    return missions;
  }
  public List<Mission> getAllMissionsAvailableForParticipant(List<Mission> missionsToCheck, String username){
    List<Mission> missions = new ArrayList<Mission>();

    return missions;
  }
}
