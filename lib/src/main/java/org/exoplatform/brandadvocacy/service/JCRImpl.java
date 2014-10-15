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
package org.exoplatform.brandadvocacy.service;

import org.exoplatform.brandadvocacy.jcr.*;
import org.exoplatform.brandadvocacy.model.*;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.distribution.DataDistributionManager;
import org.exoplatform.services.jcr.ext.distribution.DataDistributionMode;
import org.exoplatform.services.jcr.ext.distribution.DataDistributionType;
import org.exoplatform.services.jcr.impl.core.query.QueryImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.LinkedList;
import java.util.List;



/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Sep 9, 2014  
 */
public class JCRImpl implements IService {

  private OrganizationService orgService;
  private RepositoryService repositoryService;
  private SessionProviderService sessionService;
  private DataDistributionManager dataDistributionManager;  
  private MissionDAO missionDAO;
  private ManagerDAO managerDAO;
  private AddressDAO addressDAO;
  private ParticipantDAO participantDAO;
  private MissionParticipantDAO missionParticipantDAO;
  private PropositionDAO propositionDAO;
  
  private static final Log log = ExoLogger.getLogger(JCRImpl.class);

  public static String workspace = "collaboration";
  
  public static final String EXTENSION_PATH = "/BrandAdvocacys";
  public static final String MISSIONS_PATH   = "Missions";
  public static final String MISSION_PARTICIPANT_PATH = "MissionParticipants";
  public static final String PARTICIPANT_PATH = "Participants";

  public static final String MISSION_NODE_TYPE = "brad:mission";
  public static final String MANAGER_LIST_NODE_TYPE = "brad:managerslist";
  public static final String PROPOSITION_LIST_NODE_TYPE = "brad:propositionslist";
  public static final String MANAGER_NODE_TYPE = "brad:manager";
  public static final String PROPOSITION_NODE_TYPE = "brad:proposition";
  public static final String PARTICIPANT_NODE_TYPE = "brad:participant";
  public static final String ADDRESS_LIST_NODE_TYPE = "brad:addresseslist";
  public static final String ADDRESS_NODE_TYPE = "brad:address";
  public static final String MISSION_PARTICIPANT_NODE_TYPE = "brad:mission-participant";
  
  public static final String APP_PATH = "ApplicationData/brandAdvocacyExtension";
  
  public JCRImpl(InitParams params, OrganizationService orgService, SessionProviderService sessionService, RepositoryService repositoryService, DataDistributionManager dataDistributionManager){

    if(params != null){
      ValueParam param = params.getValueParam("workspace");
      if(param != null){
        workspace = param.getValue();
      }
    }
    this.setMissionDAO(new MissionDAO(this));
    this.setManagerDAO(new ManagerDAO(this));
    this.setParticipantDAO(new ParticipantDAO(this));
    this.setMissionParticipantDAO(new MissionParticipantDAO(this));
    this.setPropositionDAO(new PropositionDAO(this));
    this.setAddressDAO(new AddressDAO(this));
    this.orgService = orgService;
    this.sessionService = sessionService;
    this.dataDistributionManager = dataDistributionManager;
    this.repositoryService = repositoryService;
    
    this.getOrCreateExtensionHome();
  }
  public Session getSession() throws RepositoryException {
    ManageableRepository repo = repositoryService.getCurrentRepository();
    SessionProvider sessionProvider = sessionService.getSystemSessionProvider(null); 
    return sessionProvider.getSession(workspace, repo);
  }
  public Node getOrCreateNode(String path) {
    try {
      Session session = getSession();    
      DataDistributionType type = dataDistributionManager.getDataDistributionType(DataDistributionMode.NONE);
      return type.getOrCreateDataNode(session.getRootNode(), path);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }
  public List<Node> getNodeByQuery(String strQuery, int offset, int limit) throws RepositoryException {
    StringBuilder sql = new StringBuilder(strQuery);
    Session session = this.getSession();
    QueryImpl jcrQuery = (QueryImpl) session.getWorkspace().getQueryManager().createQuery(sql.toString(), javax.jcr.query.Query.SQL);
    if (limit >= 0) {
        jcrQuery.setOffset(offset);
        jcrQuery.setLimit(limit);
    }
    NodeIterator results = jcrQuery.execute().getNodes();

    List<Node> nodes = new LinkedList<Node>();
    while (results.hasNext()) {
        nodes.add(results.nextNode());
    }
    return nodes;
  }  
  public Node getOrCreateExtensionHome(){
    String path = String.format("%s", EXTENSION_PATH);
    return this.getOrCreateNode(path);
  }
  public MissionDAO getMissionDAO() {
    return missionDAO;
  }
  public void setMissionDAO(MissionDAO missionDAO) {
    this.missionDAO = missionDAO;
  }
  public ManagerDAO getManagerDAO() {
    return managerDAO;
  }
  public void setManagerDAO(ManagerDAO managerDAO) {
    this.managerDAO = managerDAO;
  }
  public ParticipantDAO getParticipantDAO() {
    return participantDAO;
  }
  public void setParticipantDAO(ParticipantDAO participantDAO) {
    this.participantDAO = participantDAO;
  }
  public AddressDAO getAddressDAO() {
    return addressDAO;
  }

  public void setAddressDAO(AddressDAO addressDAO) {
    this.addressDAO = addressDAO;
  }
  public MissionParticipantDAO getMissionParticipantDAO() {
    return missionParticipantDAO;
  }
  public void setMissionParticipantDAO(MissionParticipantDAO missionParticipantDAO) {
    this.missionParticipantDAO = missionParticipantDAO;
  }
  public PropositionDAO getPropositionDAO() {
    return propositionDAO;
  }
  public void setPropositionDAO(PropositionDAO propositionDAO) {
    this.propositionDAO = propositionDAO;
  }

  
  @Override
  public Mission addMission(Mission m) throws BrandAdvocacyServiceException{
      return this.getMissionDAO().createMission(m);
  }

  @Override
  public void removeMission(String id) {

    this.getMissionDAO().removeMissionById(id);
    
  }

  @Override
  public Mission getMissionById(String id){
    return this.getMissionDAO().getMissionById(id);
  }

  @Override
  public List<Mission> getAllMissions() {
    return this.getMissionDAO().getAllMissions();
  }

  @Override
  public Mission updateMission(Mission m) {
    return this.getMissionDAO().updateMission(m);
  }

  @Override
  public Mission getRandomMisson(int priority) {
    return this.getMissionDAO().getRandomMission(priority);
  }

  @Override
  public List<Mission> getAllMissionsByParticipant(String username) {
    return this.getMissionDAO().getAllMissionsByParticipant(username);
  }

  @Override
  public Mission addManagers2Mission(String mid, List<Manager> managers) {
    return this.getManagerDAO().addManager2Mission(mid,managers);
  }

  @Override
  public Mission addProposition2Mission(String mid, List<Proposition> propositions) {
    return this.getPropositionDAO().addProposition2Mission(mid,propositions);
  }

  @Override
  public void removeProposition(String id) {
    this.getPropositionDAO().removeProposition(id);
  }

  @Override
  public MissionParticipant addMissionParticipant(MissionParticipant missionParticipant) {
    return this.getMissionParticipantDAO().addMissionParticipant(missionParticipant);
  }

  @Override
  public MissionParticipant updateMissionParticipant(MissionParticipant missionParticipant) {
    return this.getMissionParticipantDAO().updateMissionParticipant(missionParticipant);
  }

  @Override
  public List<MissionParticipant> getAllMissionParticipants() {
    return this.getMissionParticipantDAO().getAllMissionParticipants();
  }

  @Override
  public List<MissionParticipant> getMissionParticipantsByParticipant(String username) {
    return this.getMissionParticipantDAO().getAllMissionParticipantsByParticipant(username);
  }

  @Override
  public MissionParticipant getMissionParticipantById(String mpId) {
    return this.getMissionParticipantDAO().getMissionParticipantById(mpId);
  }

  @Override
  public Participant addParticipant(Participant p){

    return this.getParticipantDAO().addParticipant(p);
    
  }

  @Override
  public void removeParticipant(String id) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public Participant getParticipantByUserName(String username) {
    return this.getParticipantDAO().getParticipantByUserName(username);
  }

  @Override
  public List<Participant> getAllParticipants() {
    return this.getParticipantDAO().getAllParticipants();
  }

  @Override
  public List<Participant> getParticipantsByMissionId(String mid) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Address addAddress(String username, Address address) {
    return this.getAddressDAO().addAddress2Participant(username,address);
  }

  @Override
  public Address updateAddress(Address address) {
    return this.getAddressDAO().updateAddress(address);
  }

  @Override
  public Address removeAddress(Address address) {
    return this.getAddressDAO().removeAddress(address);
  }

  @Override
  public List<Address> getAllAddressesByParticipant(String username) {
    return this.getAddressDAO().getAllAddressesByParticipant(username);
  }

  @Override
  public Manager updateManager(Manager manager) {
    return this.getManagerDAO().updateManager(manager);
  }

  @Override
  public List<Manager> getAllManagers(String mid){
    return this.getManagerDAO().getAllManagers(mid);
  }

  @Override
  public void removeManager(String missionLabelId, String username) {
    this.getManagerDAO().removeManager(missionLabelId,username);
  }

  @Override
  public Manager getManager(String missionLabelId, String username) {
    return this.getManagerDAO().getManager(missionLabelId,username);
  }

  @Override
  public Proposition updateProposition(Proposition proposition) {
    return this.getPropositionDAO().updateProposition(proposition);
  }

  @Override
  public Proposition getPropositionById(String id){
    return this.getPropositionDAO().getPropositionById(id);
  }
  @Override
  public List<Proposition> getPropositionsByMissionId(String mid) {
    return this.getPropositionDAO().getAllPropositions(mid);
  }

  @Override
  public List<Proposition> searchPropositions(String sql){
    return this.getPropositionDAO().searchPropositions(sql);
  }

  @Override
  public Proposition getRandomProposition(String mid) {
    return this.getPropositionDAO().getRandomProposition(mid);
  }

}
