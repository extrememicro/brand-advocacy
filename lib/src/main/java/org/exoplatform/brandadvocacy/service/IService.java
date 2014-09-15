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

import java.util.List;

import org.exoplatform.brandadvocacy.model.Mission;
import org.exoplatform.brandadvocacy.model.Participant;
import org.exoplatform.brandadvocacy.model.ParticipantMission;
import org.exoplatform.brandadvocacy.model.Proposition;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Sep 9, 2014  
 */
public interface IService {
 
  public void addMission(Mission m);
  public void removeMission(String id);
  public Mission getMissionById(String id);
  public List<Mission> getAllMissions();
  public void updateMission(Mission m);
  
  public void addParticipant(Participant p);
  public void removeParticipant(String id);
  public Participant getParticipantById(String id);
  public List<Participant> getAllParticipants();
  public List<Participant> getParticipantsByMissionId(String mid);

  public void addParticipantMission(ParticipantMission pm);
  public ParticipantMission getParticipantMissionById(String id);
  public List<ParticipantMission> getParticipantMissionsByParticipantId(String pid);
  public void updateParticipantMission(ParticipantMission pm);
  
  public void addProposition(Proposition p);
  public void removeProposition(String id);
  public Proposition getPropositionById(String id);
  public List<Proposition> getPropositionsByMissionId(String mid);
}
