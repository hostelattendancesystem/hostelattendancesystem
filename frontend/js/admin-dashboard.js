// API Configuration
const API_BASE_URL = 'http://localhost:8081/api';

// Check admin authentication
const adminAuth = sessionStorage.getItem('adminAuth');
const userType = sessionStorage.getItem('userType');

if (!adminAuth || userType !== 'admin') {
    window.location.href = 'admin-auth.html';
}

// DOM Elements
const adminLogoutBtn = document.getElementById('adminLogoutBtn');
const searchBtn = document.getElementById('searchBtn');
const clearSearchBtn = document.getElementById('clearSearchBtn');
const refreshBtn = document.getElementById('refreshBtn');
const searchInput = document.getElementById('searchInput');
const searchType = document.getElementById('searchType');
const studentsTableBody = document.getElementById('studentsTableBody');
const editStudentModal = document.getElementById('editStudentModal');
const adminMessage = document.getElementById('adminMessage');

// Student data
let allStudents = [];
let filteredStudents = [];

// Load all students on page load
loadAllStudents();

// Logout
adminLogoutBtn.addEventListener('click', () => {
    if (confirm('Are you sure you want to logout?')) {
        sessionStorage.clear();
        window.location.href = 'admin-auth.html';
    }
});

// Refresh Button
refreshBtn.addEventListener('click', () => {
    loadAllStudents();
});

// Search Button
searchBtn.addEventListener('click', () => {
    performSearch();
});

// Search on Enter
searchInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
        performSearch();
    }
});

// Clear Search
clearSearchBtn.addEventListener('click', () => {
    searchInput.value = '';
    clearSearchBtn.style.display = 'none';
    filteredStudents = allStudents;
    displayStudents();
});

// Load All Students
async function loadAllStudents() {
    try {
        showLoading();
        
        const response = await fetch(`${API_BASE_URL}/students`);
        
        if (response.ok) {
            allStudents = await response.json();
            filteredStudents = allStudents;
            displayStudents();
            updateStats();
        } else {
            showToast('Failed to load students', 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        showToast('Network error. Please refresh the page.', 'error');
    }
}

// Perform Search
function performSearch() {
    const query = searchInput.value.trim().toLowerCase();
    const type = searchType.value;
    
    if (!query) {
        showToast('Please enter a search term', 'error');
        return;
    }
    
    if (type === 'sic') {
        filteredStudents = allStudents.filter(student => 
            student.sic.toLowerCase().includes(query)
        );
    } else if (type === 'email') {
        filteredStudents = allStudents.filter(student => 
            student.email.toLowerCase().includes(query)
        );
    }
    
    displayStudents();
    clearSearchBtn.style.display = 'inline-block';
    
    if (filteredStudents.length === 0) {
        showToast('No students found matching your search', 'info');
    }
}

// Display Students
function displayStudents() {
    if (filteredStudents.length === 0) {
        studentsTableBody.innerHTML = `
            <tr>
                <td colspan="11" class="loading-cell">
                    <span>No students found</span>
                </td>
            </tr>
        `;
        return;
    }
    
    studentsTableBody.innerHTML = filteredStudents.map(student => `
        <tr>
            <td>${student.slNo}</td>
            <td>${student.sic}</td>
            <td>${student.email}</td>
            <td>${student.attendanceCount}</td>
            <td>
                <span class="status-badge ${student.status.toLowerCase()}">
                    ${student.status}
                </span>
            </td>
            <td>${student.isTaken ? '✅ Yes' : '❌ No'}</td>
            <td>${student.isVerified ? '✅ Yes' : '❌ No'}</td>
            <td>${student.pauseTill ? new Date(student.pauseTill).toLocaleDateString('en-IN') : '-'}</td>
            <td>${student.takenOn ? new Date(student.takenOn).toLocaleString('en-IN', {year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'}) : 'Never'}</td>
            <td>${new Date(student.addedOn).toLocaleDateString('en-IN')}</td>
            <td>
                <button class="action-btn" onclick="openMessageModal('${student.email}', '${student.sic}')" title="Send Message">
                    ✉️
                </button>
                <button class="action-btn" onclick="editStudent(${student.slNo})" title="Edit Student">
                    ✏️
                </button>
            </td>
        </tr>
    `).join('');
}

// Update Stats
function updateStats() {
    document.getElementById('totalStudents').textContent = allStudents.length;
    
    const activeCount = allStudents.filter(s => s.status === 'ACTIVE').length;
    const pausedCount = allStudents.filter(s => s.status === 'PAUSED').length;
    const deactivatedCount = allStudents.filter(s => s.status === 'DEACTIVATED').length;
    
    document.getElementById('activeStudents').textContent = activeCount;
    document.getElementById('pausedStudents').textContent = pausedCount;
    document.getElementById('deactivatedStudents').textContent = deactivatedCount;
}

// Edit Student
window.editStudent = function(slNo) {
    const student = allStudents.find(s => s.slNo === slNo);
    
    if (!student) {
        showToast('Student not found', 'error');
        return;
    }
    
    // Populate form
    document.getElementById('editSlNo').value = student.slNo;
    document.getElementById('editSic').value = student.sic;
    document.getElementById('editEmail').value = student.email;
    document.getElementById('editAttendanceCount').value = student.attendanceCount;
    document.getElementById('editStatus').value = student.status;
    document.getElementById('editIsTaken').value = student.isTaken.toString();
    document.getElementById('editIsVerified').value = student.isVerified.toString();
    document.getElementById('editPauseTill').value = student.pauseTill || '';
    document.getElementById('editAddedOn').value = new Date(student.addedOn).toLocaleString('en-IN');
    
    // Show modal
    editStudentModal.classList.add('active');
};

// Close Edit Modal
document.getElementById('closeEditModal').addEventListener('click', () => {
    editStudentModal.classList.remove('active');
});

document.getElementById('cancelEditBtn').addEventListener('click', () => {
    editStudentModal.classList.remove('active');
});

// Edit Student Form Submission
document.getElementById('editStudentForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const slNo = document.getElementById('editSlNo').value;
    const sic = document.getElementById('editSic').value.trim();
    const email = document.getElementById('editEmail').value.trim();
    const attendanceCount = parseInt(document.getElementById('editAttendanceCount').value);
    const status = document.getElementById('editStatus').value;
    const isTaken = document.getElementById('editIsTaken').value === 'true';
    const isVerified = document.getElementById('editIsVerified').value === 'true';
    const pauseTill = document.getElementById('editPauseTill').value || null;
    
    const student = allStudents.find(s => s.slNo == slNo);
    
    if (!student) {
        showToast('Student not found', 'error');
        return;
    }
    
    setModalLoading(true);
    
    try {
        // Prepare update data
        const updateData = {
            slNo: parseInt(slNo),
            sic: sic,
            email: email,
            attendanceCount: attendanceCount,
            status: status,
            isTaken: isTaken,
            isVerified: isVerified,
            addedOn: student.addedOn
        };
        
        // Clear pauseTill if changing to ACTIVE or DEACTIVATED
        if (status === 'ACTIVE' || status === 'DEACTIVATED') {
            updateData.pauseTill = null;
        } else {
            updateData.pauseTill = pauseTill;
        }
        
        const response = await fetch(`${API_BASE_URL}/students/${student.sic}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(updateData)
        });
        
        if (response.ok) {
            showToast('Student updated successfully!', 'success');
            editStudentModal.classList.remove('active');
            loadAllStudents();
        } else {
            const errorText = await response.text();
            showToast(errorText || 'Failed to update student', 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        showToast('Update failed. Please try again.', 'error');
    } finally {
        setModalLoading(false);
    }
});

// Helper Functions
function showLoading() {
    studentsTableBody.innerHTML = `
        <tr>
            <td colspan="11" class="loading-cell">
                <div class="loading-spinner"></div>
                <span>Loading students...</span>
            </td>
        </tr>
    `;
}

function showToast(message, type) {
    adminMessage.textContent = message;
    adminMessage.className = `toast-message ${type}`;
    adminMessage.style.display = 'block';
    
    setTimeout(() => {
        adminMessage.style.display = 'none';
    }, 5000);
}

function setModalLoading(isLoading) {
    const form = document.getElementById('editStudentForm');
    const btn = form.querySelector('button[type="submit"]');
    const btnText = btn.querySelector('.btn-text');
    const btnLoader = btn.querySelector('.btn-loader');
    
    if (isLoading) {
        btnText.style.display = 'none';
        btnLoader.style.display = 'inline';
        btn.disabled = true;
    } else {
        btnText.style.display = 'inline';
        btnLoader.style.display = 'none';
        btn.disabled = false;
    }
}

// ========== MESSAGING FUNCTIONALITY ==========

// Message Modal Elements
const messageModal = document.getElementById('messageModal');
const sendMessageBtn = document.getElementById('sendMessageBtn');
const sendToAllCheckbox = document.getElementById('sendToAll');
const emailGroup = document.getElementById('emailGroup');
const messageEmail = document.getElementById('messageEmail');
const messageSubject = document.getElementById('messageSubject');
const messageBody = document.getElementById('messageBody');

// Open message modal from header (to all)
sendMessageBtn.addEventListener('click', () => {
    openMessageModal(null, null, true);
});

// Open message modal for specific student or all students
window.openMessageModal = function(email, sic, toAll = false) {
    messageModal.classList.add('active');
    
    if (toAll) {
        sendToAllCheckbox.checked = true;
        emailGroup.style.display = 'none';
        messageEmail.required = false;
    } else {
        sendToAllCheckbox.checked = false;
        emailGroup.style.display = 'block';
        messageEmail.value = email || '';
        messageEmail.required = true;
    }
    
    messageSubject.value = '';
    messageBody.value = '';
};

// Toggle send to all
sendToAllCheckbox.addEventListener('change', (e) => {
    if (e.target.checked) {
        emailGroup.style.display = 'none';
        messageEmail.required = false;
    } else {
        emailGroup.style.display = 'block';
        messageEmail.required = true;
    }
});

// Close message modal
document.getElementById('closeMessageModal').addEventListener('click', () => {
    messageModal.classList.remove('active');
});

document.getElementById('cancelMessageBtn').addEventListener('click', () => {
    messageModal.classList.remove('active');
});

// Send message form
document.getElementById('messageForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const sendToAll = sendToAllCheckbox.checked;
    const email = messageEmail.value;
    const subject = messageSubject.value;
    const body = messageBody.value;
    
    if (!sendToAll && !email) {
        showToast('Please enter a recipient email', 'error');
        return;
    }
    
    if (!subject || !body) {
        showToast('Please fill in subject and message', 'error');
        return;
    }
    
    // Show loading state
    const form = document.getElementById('messageForm');
    const btn = form.querySelector('button[type="submit"]');
    const btnText = btn.querySelector('.btn-text');
    const btnLoader = btn.querySelector('.btn-loader');
    
    btnText.style.display = 'none';
    btnLoader.style.display = 'inline';
    btn.disabled = true;
    
    try {
        const endpoint = sendToAll ? '/messages/send-all' : '/messages/send';
        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                toEmail: email,
                subject: subject,
                body: body,
                sendToAll: sendToAll
            })
        });
        
        if (response.ok) {
            const result = await response.json();
            showToast(
                sendToAll 
                    ? `Message sent to ${result.recipientCount} students!` 
                    : 'Message sent successfully!', 
                'success'
            );
            messageModal.classList.remove('active');
            
            // Reset form
            messageSubject.value = '';
            messageBody.value = '';
            messageEmail.value = '';
            sendToAllCheckbox.checked = false;
            emailGroup.style.display = 'block';
        } else {
            const error = await response.json();
            showToast(error.message || 'Failed to send message', 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        showToast('Failed to send message. Please try again.', 'error');
    } finally {
        btnText.style.display = 'inline';
        btnLoader.style.display = 'none';
        btn.disabled = false;
    }
});

// Close modal on outside click
window.addEventListener('click', (e) => {
    if (e.target === editStudentModal) {
        editStudentModal.classList.remove('active');
    }
    if (e.target === messageModal) {
        messageModal.classList.remove('active');
    }
});
