package com.example.coursetable.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.coursetable.data.model.Schedule

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    schedules: List<Schedule>,
    activeScheduleId: Long?,
    onCreateSchedule: (String) -> Unit,
    onRenameSchedule: (Long, String) -> Unit,
    onDeleteSchedule: (Long) -> Unit,
    onSelectSchedule: (Long) -> Unit,
    onBack: () -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf<Pair<Long, String>?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<Pair<Long, String>?>(null) }
    var newScheduleName by remember { mutableStateOf("") }
    var renameText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Schedule management section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("课表管理", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Button(onClick = { newScheduleName = ""; showCreateDialog = true }) {
                        Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("新建课表")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(schedules) { schedule ->
                val isActive = schedule.id == activeScheduleId
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { onSelectSchedule(schedule.id) }.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = schedule.name, style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                                )
                                if (isActive) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "当前", style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            schedule.semester?.let {
                                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        if (!isActive) {
                            IconButton(onClick = {
                                renameText = schedule.name
                                showRenameDialog = Pair(schedule.id, schedule.name)
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Edit, contentDescription = "重命名",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        if (schedules.size > 1) {
                            IconButton(onClick = { showDeleteConfirm = Pair(schedule.id, schedule.name) }) {
                                Icon(
                                    imageVector = Icons.Filled.Delete, contentDescription = "删除",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f), modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // About section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("关于", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Filled.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("课程表", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                Text("版本 1.0.0", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "一款智能课程表管理应用，支持OCR导入课程表、多课表管理、每周课程视图等功能。",
                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("新建课表") },
            text = {
                OutlinedTextField(
                    value = newScheduleName, onValueChange = { newScheduleName = it },
                    label = { Text("课表名称") }, placeholder = { Text("例如：2024秋季学期") },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = { if (newScheduleName.isNotBlank()) { onCreateSchedule(newScheduleName.trim()); showCreateDialog = false } },
                    enabled = newScheduleName.isNotBlank()
                ) { Text("创建") }
            },
            dismissButton = { TextButton(onClick = { showCreateDialog = false }) { Text("取消") } }
        )
    }

    showRenameDialog?.let { (id, _) ->
        AlertDialog(
            onDismissRequest = { showRenameDialog = null },
            title = { Text("重命名课表") },
            text = {
                OutlinedTextField(
                    value = renameText, onValueChange = { renameText = it },
                    label = { Text("课表名称") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = { if (renameText.isNotBlank()) { onRenameSchedule(id, renameText.trim()); showRenameDialog = null } },
                    enabled = renameText.isNotBlank()
                ) { Text("确定") }
            },
            dismissButton = { TextButton(onClick = { showRenameDialog = null }) { Text("取消") } }
        )
    }

    showDeleteConfirm?.let { (id, name) ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("删除课表") },
            text = { Text("确定要删除课表「$name」吗？该课表下的所有课程也会被删除，此操作不可撤销。") },
            confirmButton = {
                Button(
                    onClick = { onDeleteSchedule(id); showDeleteConfirm = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("删除") }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = null }) { Text("取消") } }
        )
    }
}
