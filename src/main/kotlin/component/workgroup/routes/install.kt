package fr.dcproject.component.workgroup.routes

import fr.dcproject.component.workgroup.routes.CreateWorkgroup.createWorkgroup
import fr.dcproject.component.workgroup.routes.DeleteWorkgroup.deleteWorkgroup
import fr.dcproject.component.workgroup.routes.EditWorkgroup.editWorkgroup
import fr.dcproject.component.workgroup.routes.GetWorkgroup.getWorkgroup
import fr.dcproject.component.workgroup.routes.GetWorkgroups.getWorkgroups
import fr.dcproject.component.workgroup.routes.members.AddMemberToWorkgroup.addMemberToWorkgroup
import fr.dcproject.component.workgroup.routes.members.DeleteMembersOfWorkgroup.deleteMemberOfWorkgroup
import fr.dcproject.component.workgroup.routes.members.UpdateMemberOfWorkgroup.updateMemberOfWorkgroup
import io.ktor.auth.authenticate
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.routing.Routing
import org.koin.ktor.ext.get

@KtorExperimentalLocationsAPI
fun Routing.installWorkgroupRoutes() {
    authenticate(optional = true) {
        /* Workgroup */
        getWorkgroups(get(), get())
        getWorkgroup(get(), get())
        createWorkgroup(get(), get())
        editWorkgroup(get(), get())
        deleteWorkgroup(get(), get())
        /* Members */
        addMemberToWorkgroup(get(), get())
        deleteMemberOfWorkgroup(get(), get())
        updateMemberOfWorkgroup(get(), get())
    }
}
