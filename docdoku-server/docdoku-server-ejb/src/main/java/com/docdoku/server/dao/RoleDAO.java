/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.docdoku.server.dao;


import com.docdoku.core.common.User;
import com.docdoku.core.services.RoleNotFoundException;
import com.docdoku.core.workflow.Role;
import com.docdoku.core.workflow.RoleKey;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;
import java.util.Locale;

/**
 * @author Morgan Guimard
 */
public class RoleDAO {

    private EntityManager em;
    private Locale mLocale;

    public RoleDAO(Locale pLocale, EntityManager pEM) {
        mLocale=pLocale;
        em=pEM;
    }

    public RoleDAO(EntityManager pEM) {
        mLocale=Locale.getDefault();
        em=pEM;
    }

    public Role loadRole(RoleKey roleKey) throws RoleNotFoundException {

        Role role = em.find(Role.class, roleKey);
        if (role == null) {
            throw new RoleNotFoundException(mLocale, roleKey);
        } else {
            return role;
        }

    }

    public List<Role> findRolesInWorkspace(String pWorkspaceId){
        return em.createNamedQuery("Role.findByWorkspace").setParameter("workspaceId", pWorkspaceId).getResultList();
    }

    public void createRole(Role pRole) {
        em.persist(pRole);
        em.flush();
    }

    public void deleteRole(Role pRole){
        em.remove(pRole);
        em.flush();
    }

    public boolean isRoleInUseInWorkflowModel(Role role) {
        return em.createNamedQuery("TaskModel.findByRoleName").setParameter("roleName", role.getName()).getResultList().size() > 0;

    }

    public List<Role> findRolesInUseWorkspace(String pWorkspaceId) {
        return em.createNamedQuery("Role.findRolesInUse").setParameter("workspaceId", pWorkspaceId).getResultList();
    }

    public void removeUserFromRoles(User pUser) {
        Query query = em.createQuery("UPDATE Role r SET r.defaultUserMapped = NULL WHERE r.defaultUserMapped = :user");
        query.setParameter("user", pUser).executeUpdate();
    }
}
