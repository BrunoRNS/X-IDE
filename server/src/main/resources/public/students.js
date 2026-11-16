AOS.init({
    duration: 700,
    easing: 'ease-out-cubic',
    once: true,
    offset: 40
});

const studentModal = document.getElementById('studentModal');
const studentModalContent = document.getElementById('studentModalContent');
const studentForm = document.getElementById('registerStudentForm');

let editingStudent = false;

function openStudentModal() {
    editingStudent = false;
    studentForm.reset();

    document.getElementById('studentId').value = '';
    document.getElementById('modalTitle').textContent = 'Register Student';
    document.getElementById('modalDescription').textContent = 'Add a new student to X-IDE.';
    document.getElementById('submitText').textContent = 'Register';
    document.getElementById('submitIcon').className = 'fa-solid fa-plus mr-1';
    studentForm.action = '/students/register';

    showModal();
}

function editStudent(button) {
    editingStudent = true;

    const { id, name, email } = button.dataset;
    document.getElementById('studentId').value = id;
    document.getElementById('studentName').value = name;
    document.getElementById('studentEmail').value = email;
    document.getElementById('modalTitle').textContent = 'Edit Student';
    document.getElementById('modalDescription').textContent = 'Update this student information.';
    document.getElementById('submitText').textContent = 'Save Changes';
    document.getElementById('submitIcon').className = 'fa-solid fa-floppy-disk mr-1';
    studentForm.action = `/students/update/${encodeURIComponent(id)}`;

    showModal();
}

function showModal() {
    studentModal.classList.remove('hidden');
    studentModal.classList.add('flex');

    requestAnimationFrame(() => {
        studentModalContent.classList.remove('scale-95', 'opacity-0');
        studentModalContent.classList.add('scale-100', 'opacity-100');
    });
}

function closeStudentModal() {
    studentModalContent.classList.remove('scale-100', 'opacity-100');
    studentModalContent.classList.add('scale-95', 'opacity-0');

    setTimeout(() => {
        studentModal.classList.add('hidden');
        studentModal.classList.remove('flex');
    }, 300);
}

studentModal.addEventListener('click', (event) => {
    if (event.target === studentModal) closeStudentModal();
});

document.addEventListener('keydown', (event) => {
    if (event.key === 'Escape' && !studentModal.classList.contains('hidden')) {
        closeStudentModal();
    }
});

studentForm.addEventListener('submit', async (event) => {
    event.preventDefault();

    const payload = {
        name: document.getElementById('studentName').value,
        email: document.getElementById('studentEmail').value
    };
    const id = document.getElementById('studentId').value;
    const url = editingStudent
        ? `/students/update/${encodeURIComponent(id)}`
        : '/students/register';

    try {
        const response = await fetch(url, {
            method: editingStudent ? 'PUT' : 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            closeStudentModal();
            window.location.reload();
        } else {
            alert(editingStudent ? 'Error updating student.' : 'Error registering student.');
        }
    } catch (error) {
        console.error('Request error:', error);
        alert('Could not connect to the server.');
    }
});

async function deleteStudent(button) {
    const { id, name } = button.dataset;
    if (!confirm(`Are you sure you want to delete ${name}?`)) return;

    try {
        const response = await fetch(`/students/delete/${encodeURIComponent(id)}`, {
            method: 'DELETE',
            headers: { 'Accept': 'application/json' }
        });

        if (response.ok) {
            window.location.reload();
        } else {
            alert('Error deleting student.');
        }
    } catch (error) {
        console.error('Delete error:', error);
        alert('Could not connect to the server.');
    }
}
