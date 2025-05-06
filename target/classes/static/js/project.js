function editAssignment(id) {
    fetch(`/project-employee/${id}`)
        .then(response => response.json())
        .then(data => {
            document.querySelector('select[name="projectId"]').value = data.projectId;
            document.querySelector('select[name="employeeId"]').value = data.employeeId;
            document.querySelector('input[name="assignedDate"]').value = data.assignedDate;
            document.querySelector('input[name="role"]').value = data.role;

            const form = document.querySelector('form');
            form.action = `/project-employee/update/${id}`;
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Failed to load assignment details');
        });
}

function deleteAssignment(id) {
    if (confirm('Are you sure you want to delete this assignment?')) {
        fetch(`/project-employee/delete/${id}`, {
            method: 'DELETE'
        })
            .then(response => {
                if (response.ok) {
                    window.location.reload();
                } else {
                    alert('Failed to delete assignment');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Failed to delete assignment');
            });
    }
}

function editProject(id) {
    fetch(`/projects/${id}`)
        .then(response => response.json())
        .then(data => {
            document.querySelector('input[name="projectName"]').value = data.projectName;
            document.querySelector('input[name="clientName"]').value = data.clientName;
            document.querySelector('input[name="startDate"]').value = data.startDate;
            document.querySelector('select[name="leadEmployeeId"]').value = data.leadEmployeeId;
            document.querySelector('input[name="contactPerson"]').value = data.contactPerson;
            document.querySelector('input[name="contactNo"]').value = data.contactNo;
            document.querySelector('input[name="emailId"]').value = data.emailId;

            const form = document.querySelector('form');
            form.action = `/projects/update/${id}`;

            const modal = new bootstrap.Modal(document.getElementById('newProjectModal'));
            modal.show();
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Failed to load project details');
        });
}

function deleteProject(id) {
    if (confirm('Are you sure you want to delete this project?')) {
        fetch(`/projects/delete/${id}`, {
            method: 'DELETE'
        })
            .then(response => {
                if (response.ok) {
                    window.location.reload();
                } else {
                    alert('Failed to delete project');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('Failed to delete project');
            });
    }
}

function openEditProjectModal(projectId) {
    $.ajax({
        url: '/projects/edit/' + projectId,
        type: 'GET',
        success: function(response) {
            // Load modal content
            $('#editProjectModal').html(response);
            // Show modal
            $('#editProjectModal').modal('show');
        },
        error: function(xhr, status, error) {
            Swal.fire({
                icon: 'error',
                title: 'Error',
                text: 'Failed to load project details'
            });
        }
    });
}

// Add event listener for modal close
$(document).ready(function() {
    $('#editProjectModal').on('hidden.bs.modal', function () {
        // Redirect back to projects page when modal is closed
        window.location.href = '/projects';
    });
});