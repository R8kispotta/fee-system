package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ExpenseItem
import com.example.data.local.FeePayment
import com.example.data.local.Student
import com.example.data.model.InstituteSettings
import com.example.ui.components.PrintReceiptHelper
import com.example.ui.theme.OverdueRed
import com.example.ui.theme.PaidGreen
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun LedgerScreen(
    payments: List<FeePayment>,
    expenses: List<ExpenseItem> = emptyList(),
    students: List<Student>,
    settings: InstituteSettings,
    onReceiptClick: (FeePayment) -> Unit,
    onCollectFeeClick: () -> Unit,
    onAddExpenseClick: () -> Unit = {},
    onDeleteExpense: (ExpenseItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedMonthFilter by remember { mutableStateOf("All") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: All, 1: Income, 2: Expenses
    var expenseToDelete by remember { mutableStateOf<ExpenseItem?>(null) }
    var isSearchVisible by remember { mutableStateOf(searchQuery.isNotEmpty()) }
    var isSummaryExpanded by remember { mutableStateOf(false) }

    fun getExpenseMonthYear(expense: ExpenseItem): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            val date = parser.parse(expense.dateString)
            if (date != null) formatter.format(date) else ""
        } catch (_: Exception) {
            val year = expense.monthKey / 100
            val month = expense.monthKey % 100
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, (month - 1).coerceAtLeast(0))
            SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
        }
    }

    val monthOptions = remember(payments, expenses) {
        val paymentMonths = payments.map { it.monthYear }
        val expenseMonths = expenses.map { getExpenseMonthYear(it) }
        listOf("All") + (paymentMonths + expenseMonths).filter { it.isNotBlank() }.distinct()
    }

    val filteredPayments = remember(payments, searchQuery, selectedMonthFilter) {
        payments.filter { p ->
            val matchesQuery = searchQuery.isBlank() ||
                    p.studentName.contains(searchQuery, ignoreCase = true) ||
                    p.receiptNo.contains(searchQuery, ignoreCase = true) ||
                    p.transactionId.contains(searchQuery, ignoreCase = true)

            val matchesMonth = selectedMonthFilter == "All" || p.monthYear == selectedMonthFilter

            matchesQuery && matchesMonth
        }
    }

    val filteredExpenses = remember(expenses, searchQuery, selectedMonthFilter) {
        expenses.filter { e ->
            val matchesQuery = searchQuery.isBlank() ||
                    e.title.contains(searchQuery, ignoreCase = true) ||
                    e.category.contains(searchQuery, ignoreCase = true) ||
                    e.notes.contains(searchQuery, ignoreCase = true)

            val matchesMonth = selectedMonthFilter == "All" || getExpenseMonthYear(e) == selectedMonthFilter

            matchesQuery && matchesMonth
        }
    }

    val totalIncome = remember(filteredPayments) {
        filteredPayments.sumOf { it.amountPaid }
    }

    val totalExpense = remember(filteredExpenses) {
        filteredExpenses.sumOf { it.amount }
    }

    val netProfit = totalIncome - totalExpense

    if (expenseToDelete != null) {
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            title = { Text("Delete Expense?") },
            text = { Text("Are you sure you want to delete '${expenseToDelete!!.title}' (${settings.currencySymbol}${expenseToDelete!!.amount.toInt()})?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteExpense(expenseToDelete!!)
                        expenseToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OverdueRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { expenseToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FloatingActionButton(
                    onClick = onAddExpenseClick,
                    containerColor = Color(0xFFFEE2E2),
                    contentColor = Color(0xFFB91C1C),
                    modifier = Modifier
                        .height(40.dp)
                        .testTag("ledger_add_expense_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Payments, contentDescription = "Add Expense", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Expense", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                    }
                }

                FloatingActionButton(
                    onClick = onCollectFeeClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .height(40.dp)
                        .testTag("ledger_collect_fee_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Collect Fee", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Collect Fee", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Pinned Compact TabRow (36dp height)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("All (${filteredPayments.size + filteredExpenses.size})", fontSize = 11.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.height(36.dp)
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Income (${filteredPayments.size})", fontSize = 11.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.height(36.dp)
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Expenses (${filteredExpenses.size})", fontSize = 11.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.height(36.dp)
                )
            }

            // Horizontal Scrollable Options Strip (Search, Months, Quick Cashflow Pill)
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSearchVisible) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .height(28.dp)
                            .clickable { isSearchVisible = !isSearchVisible }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                modifier = Modifier.size(13.dp),
                                tint = if (isSearchVisible) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Search", fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (netProfit >= 0) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                        modifier = Modifier
                            .height(28.dp)
                            .clickable { isSummaryExpanded = !isSummaryExpanded }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = "Net: ${if (netProfit >= 0) "+" else ""}${settings.currencySymbol}${netProfit.toInt()}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (netProfit >= 0) Color(0xFF15803D) else Color(0xFFB91C1C)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isSummaryExpanded) "▲" else "▼",
                                fontSize = 8.sp,
                                color = if (netProfit >= 0) Color(0xFF15803D) else Color(0xFFB91C1C)
                            )
                        }
                    }
                }

                items(monthOptions) { month ->
                    FilterChip(
                        selected = selectedMonthFilter == month,
                        onClick = { selectedMonthFilter = month },
                        label = { Text(month, fontSize = 10.sp) },
                        modifier = Modifier.height(28.dp)
                    )
                }
            }

            // Animated Collapsible Search Field
            AnimatedVisibility(visible = isSearchVisible) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search receipt, student, expense...", fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(14.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(20.dp)) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(13.dp))
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                        .height(38.dp)
                        .testTag("ledger_search_bar"),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
            }

            // Animated Collapsible Detailed Cashflow Summary Card
            AnimatedVisibility(visible = isSummaryExpanded) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Cashflow ($selectedMonthFilter)",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${settings.currencySymbol}${netProfit.toInt()}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (netProfit >= 0) PaidGreen else OverdueRed
                            )
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Collections: +${settings.currencySymbol}${totalIncome.toInt()} (${filteredPayments.size})",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PaidGreen
                            )

                            Text(
                                "Expenses: -${settings.currencySymbol}${totalExpense.toInt()} (${filteredExpenses.size})",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OverdueRed
                            )
                        }
                    }
                }
            }

            // Scrollable Content (Transactions)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 72.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {

                // Content List
                val showPayments = selectedTab == 0 || selectedTab == 1
                val showExpenses = selectedTab == 0 || selectedTab == 2
                val totalItems = (if (showPayments) filteredPayments.size else 0) + (if (showExpenses) filteredExpenses.size else 0)

                if (totalItems == 0) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Receipt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "No records found",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                } else {
                    if (showPayments) {
                        items(filteredPayments, key = { "pay_${it.id}" }) { payment ->
                            val student = students.find { it.id == payment.studentId }
                            LedgerCardItem(
                                payment = payment,
                                student = student,
                                settings = settings,
                                onClick = { onReceiptClick(payment) },
                                onPrint = { PrintReceiptHelper.printReceipt(context, payment, student, settings) },
                                onShare = { PrintReceiptHelper.shareReceiptText(context, payment, student, settings) }
                            )
                        }
                    }

                    if (showExpenses) {
                        items(filteredExpenses, key = { "exp_${it.id}" }) { expense ->
                            ExpenseCardItem(
                                expense = expense,
                                currencySymbol = settings.currencySymbol,
                                onDelete = { expenseToDelete = expense }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LedgerCardItem(
    payment: FeePayment,
    student: Student?,
    settings: InstituteSettings,
    onClick: () -> Unit,
    onPrint: () -> Unit,
    onShare: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(payment.paymentDate))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = payment.receiptNo,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = "+${settings.currencySymbol}${payment.amountPaid.toInt()}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = PaidGreen
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${payment.studentName} • ${payment.gradeClass}",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )

            Text(
                text = "${payment.monthYear}${if (payment.monthsCovered > 1) " (${payment.monthsCovered} Mo)" else ""} • ${payment.paymentMode}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedDate,
                    fontSize = 9.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row {
                    IconButton(onClick = onPrint, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = "Print", modifier = Modifier.size(14.dp))
                    }
                    IconButton(onClick = onShare, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseCardItem(
    expense: ExpenseItem,
    currencySymbol: String,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFFEE2E2)
                    ) {
                        Text(
                            text = expense.category,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB91C1C),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = expense.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Text(
                    text = "-${currencySymbol}${expense.amount.toInt()}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.5.sp,
                    color = OverdueRed
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${expense.dateString} • ${expense.paymentMode}${if (expense.notes.isNotEmpty()) " • ${expense.notes}" else ""}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                IconButton(onClick = onDelete, modifier = Modifier.size(26.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Expense",
                        tint = OverdueRed.copy(alpha = 0.7f),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}
