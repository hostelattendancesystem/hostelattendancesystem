// API Configuration
const API_BASE_URL = 'https://hostelattendancesystem.onrender.com/api';

// Check authentication
const userSic = sessionStorage.getItem('userSic');
const userType = sessionStorage.getItem('userType');

if (!userSic || userType !== 'student') {
    window.location.href = 'user-auth.html';
}

// DOM Elements
const logoutBtn = document.getElementById('logoutBtn');
const editEmailBtn = document.getElementById('editEmailBtn');
const editPauseBtn = document.getElementById('editPauseBtn');
const editStatusBtn = document.getElementById('editStatusBtn');
const deactivateBtn = document.getElementById('deactivateBtn');
const editEmailModal = document.getElementById('editEmailModal');
const editPauseModal = document.getElementById('editPauseModal');
const editStatusModal = document.getElementById('editStatusModal');
const dashboardMessage = document.getElementById('dashboardMessage');

// Student data
let studentData = null;

// Load student data on page load
loadStudentData();

// Logout
logoutBtn.addEventListener('click', () => {
    if (confirm('Are you sure you want to logout?')) {
        sessionStorage.clear();
        window.location.href = 'user-auth.html';
    }
});

// Load Student Data
async function loadStudentData() {
    try {
        const response = await fetch(`${API_BASE_URL}/students/${userSic}`);
        
        if (response.ok) {
            studentData = await response.json();
            displayStudentData();
        } else {
            showToast('Failed to load student data', 'error');
            setTimeout(() => {
                sessionStorage.clear();
                window.location.href = 'user-auth.html';
            }, 2000);
        }
    } catch (error) {
        console.error('Error:', error);
        showToast('Network error. Please refresh the page.', 'error');
    }
}

// Display Student Data
function displayStudentData() {
    // Header
    document.getElementById('headerUserName').textContent = studentData.sic;
    document.getElementById('userName').textContent = studentData.sic;
    
    // Stats
    document.getElementById('statAttendance').textContent = studentData.attendanceCount;
    
    const addedDate = new Date(studentData.addedOn);
    document.getElementById('statRegistered').textContent = addedDate.toLocaleDateString('en-IN', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
    });
    
    // Status
    const statusElement = document.getElementById('statStatus');
    statusElement.textContent = studentData.status;
    statusElement.className = 'stat-value status-badge ' + studentData.status.toLowerCase();
    
    // Today's attendance
    const todayElement = document.getElementById('statToday');
    
    if (studentData.isTaken) {
        if (studentData.isVerified) {
            todayElement.textContent = 'Marked & Verified';
            todayElement.style.color = 'var(--success)';
        } else {
            todayElement.textContent = 'Marked (Pending)';
            todayElement.style.color = 'var(--warning)';
        }
    } else {
        todayElement.textContent = 'Not Marked';
        todayElement.style.color = 'var(--text-muted)';
    }
    
    // Profile details
    document.getElementById('profileSic').textContent = studentData.sic;
    document.getElementById('profileEmail').textContent = studentData.email;
    document.getElementById('profileVerified').textContent = studentData.isVerified ? 'Yes' : 'No';
    
    // Profile status
    const profileStatusElement = document.getElementById('profileStatus');
    profileStatusElement.textContent = studentData.status;
    profileStatusElement.className = 'profile-value status-badge ' + studentData.status.toLowerCase();
    
    if (studentData.pauseTill) {
        const pauseDate = new Date(studentData.pauseTill);
        document.getElementById('profilePause').textContent = pauseDate.toLocaleDateString('en-IN');
    } else {
        document.getElementById('profilePause').textContent = 'Not Set';
    }
    
    // Display last attendance taken timestamp
    if (studentData.takenOn) {
        const takenDate = new Date(studentData.takenOn);
        document.getElementById('profileTakenOn').textContent = takenDate.toLocaleString('en-IN', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    } else {
        document.getElementById('profileTakenOn').textContent = 'Never';
    }
    
    // Display registered date
    document.getElementById('profileRegistered').textContent = addedDate.toLocaleDateString('en-IN', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
    });
}

// Edit Email Button
editEmailBtn.addEventListener('click', () => {
    editEmailModal.classList.add('active');
    document.getElementById('newEmail').value = '';
    document.getElementById('editEmailForm').style.display = 'block';
    document.getElementById('emailOtpSection').style.display = 'none';
});

// Close Email Modal
document.getElementById('closeEmailModal').addEventListener('click', () => {
    editEmailModal.classList.remove('active');
});

// Edit Email Form
let tempNewEmail = '';

document.getElementById('editEmailForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const newEmail = document.getElementById('newEmail').value.trim();
    
    if (!newEmail) {
        showToast('Please enter a new email address', 'error');
        return;
    }
    
    setModalLoading('editEmailForm', true);
    
    try {
        // Send OTP to new email
        const response = await fetch(`${API_BASE_URL}/auth/send-email-change-otp`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                sic: userSic,
                newEmail: newEmail
            })
        });
        
        if (response.ok) {
            tempNewEmail = newEmail;
            document.getElementById('editEmailForm').style.display = 'none';
            document.getElementById('emailOtpSection').style.display = 'block';
            showToast(`Verification OTP sent to ${newEmail}`, 'success');
        } else {
            showToast('Failed to send OTP. Email may already be in use.', 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        showToast('Network error. Please try again.', 'error');
    } finally {
        setModalLoading('editEmailForm', false);
    }
});

// Email OTP Verification
document.getElementById('emailOtpForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const otp = document.getElementById('emailOtp').value.trim();
    
    if (!otp || otp.length !== 6) {
        showToast('Please enter a valid 6-digit OTP', 'error');
        return;
    }
    
    setModalLoading('emailOtpForm', true);
    
    try {
        const response = await fetch(`${API_BASE_URL}/students/${userSic}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                ...studentData,
                email: tempNewEmail,
                otp: otp
            })
        });
        
        if (response.ok) {
            showToast('Email updated successfully!', 'success');
            editEmailModal.classList.remove('active');
            loadStudentData();
        } else {
            showToast('Invalid OTP or update failed', 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        showToast('Update failed. Please try again.', 'error');
    } finally {
        setModalLoading('emailOtpForm', false);
    }
});

// Edit Pause Date Button
editPauseBtn.addEventListener('click', () => {
    editPauseModal.classList.add('active');
    
    // Set min date to today
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('pauseDate').min = today;
    
    // If attendance is taken today, set min to tomorrow
    if (studentData.isTaken) {
        const tomorrow = new Date();
        tomorrow.setDate(tomorrow.getDate() + 1);
        document.getElementById('pauseDate').min = tomorrow.toISOString().split('T')[0];
    }
    
    // Set current value if exists
    if (studentData.pauseTill) {
        document.getElementById('pauseDate').value = studentData.pauseTill;
    }
});

// Close Pause Modal
document.getElementById('closePauseModal').addEventListener('click', () => {
    editPauseModal.classList.remove('active');
});

// Edit Pause Form
document.getElementById('editPauseForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const pauseDate = document.getElementById('pauseDate').value;
    
    if (!pauseDate) {
        showToast('Please select a date', 'error');
        return;
    }
    
    // Validate date
    const selectedDate = new Date(pauseDate);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    if (selectedDate < today) {
        showToast('Cannot select a past date', 'error');
        return;
    }
    
    if (studentData.isTaken && selectedDate.toDateString() === today.toDateString()) {
        showToast('Cannot select today as attendance is already taken', 'error');
        return;
    }
    
    setModalLoading('editPauseForm', true);
    
    try {
        const response = await fetch(`${API_BASE_URL}/students/${userSic}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                ...studentData,
                pauseTill: pauseDate,
                status: 'PAUSED'
            })
        });
        
        if (response.ok) {
            showToast('Pause date updated successfully!', 'success');
            editPauseModal.classList.remove('active');
            loadStudentData();
        } else {
            showToast('Failed to update pause date', 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        showToast('Update failed. Please try again.', 'error');
    } finally {
        setModalLoading('editPauseForm', false);
    }
});

// Deactivate Account
deactivateBtn.addEventListener('click', async () => {
    if (!confirm('Are you sure you want to deactivate your account? This action will stop all automatic attendance marking.')) {
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/students/${userSic}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                ...studentData,
                status: 'DEACTIVATED'
            })
        });
        
        if (response.ok) {
            showToast('Account deactivated successfully', 'success');
            setTimeout(() => {
                sessionStorage.clear();
                window.location.href = 'user-auth.html';
            }, 2000);
        } else {
            showToast('Failed to deactivate account', 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        showToast('Deactivation failed. Please try again.', 'error');
    }
});

// Helper Functions
function showToast(message, type) {
    dashboardMessage.textContent = message;
    dashboardMessage.className = `toast-message ${type}`;
    dashboardMessage.style.display = 'block';
    
    setTimeout(() => {
        dashboardMessage.style.display = 'none';
    }, 5000);
}

function setModalLoading(formId, isLoading) {
    const form = document.getElementById(formId);
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

// Edit Status Button
editStatusBtn.addEventListener('click', () => {
    editStatusModal.classList.add('active');
    document.getElementById('newStatus').value = studentData.status;
});

// Close Status Modal
document.getElementById('closeStatusModal').addEventListener('click', () => {
    editStatusModal.classList.remove('active');
});

// Edit Status Form
document.getElementById('editStatusForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    const newStatus = document.getElementById('newStatus').value;
    
    if (newStatus === studentData.status) {
        showToast('Status is already set to ' + newStatus, 'info');
        return;
    }
    
    setModalLoading('editStatusForm', true);
    
    try {
        // Prepare update data
        const updateData = {
            sic: studentData.sic,
            email: studentData.email,
            status: newStatus,
            attendanceCount: studentData.attendanceCount,
            isTaken: studentData.isTaken,
            isVerified: studentData.isVerified
        };
        
        // Clear pauseTill if changing to ACTIVE or DEACTIVATED
        if (newStatus === 'ACTIVE' || newStatus === 'DEACTIVATED') {
            updateData.pauseTill = null;
        } else {
            updateData.pauseTill = studentData.pauseTill;
        }
        
        const response = await fetch(`${API_BASE_URL}/students/${userSic}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(updateData)
        });
        
        if (response.ok) {
            showToast('Account status updated successfully!', 'success');
            editStatusModal.classList.remove('active');
            loadStudentData();
        } else {
            showToast('Failed to update status', 'error');
        }
    } catch (error) {
        console.error('Error:', error);
        showToast('Update failed. Please try again.', 'error');
    } finally {
        setModalLoading('editStatusForm', false);
    }
});

// Close modals on outside click
window.addEventListener('click', (e) => {
    if (e.target === editEmailModal) {
        editEmailModal.classList.remove('active');
    }
    if (e.target === editPauseModal) {
        editPauseModal.classList.remove('active');
    }
    if (e.target === editStatusModal) {
        editStatusModal.classList.remove('active');
    }
});
